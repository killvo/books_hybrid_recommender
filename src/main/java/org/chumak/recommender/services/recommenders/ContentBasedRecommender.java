package org.chumak.recommender.services.recommenders;

import lombok.RequiredArgsConstructor;
import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.feature.HashingTF;
import org.apache.spark.ml.feature.StopWordsRemover;
import org.apache.spark.ml.feature.Tokenizer;
import org.apache.spark.ml.feature.VectorAssembler;
import org.apache.spark.ml.linalg.BLAS;
import org.apache.spark.ml.linalg.Vector;
import org.apache.spark.ml.linalg.Vectors;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.types.DataTypes;
import org.chumak.recommender.domain.work.WorkService;
import org.chumak.recommender.domain.work.dto.WorkDto;
import org.chumak.recommender.services.recommendation.dto.RecommendationDto;
import org.chumak.recommender.services.saver.RecommendationSaverService;
import org.chumak.recommender.services.saver.common.RecommendationTable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContentBasedRecommender {

	@Value("${recommender.content.similarityThreshold}")
	private Double SIMILARITY_THRESHOLD;

	private final WorkService workService;
	private final RecommendationSaverService recommendationSaverService;

	public void generateForUser(UUID userId, List<String> userRatedWorkIds) {
		if (userRatedWorkIds.isEmpty()) {
			System.out.println("ContentBasedRecommender: There are no user rated works to analyze.");
			return;
		}

		List<WorkDto> allWorks = workService.findAllWorksDto();

		SparkSession spark = SparkSession.builder()
			.appName("ContentBasedRecommendation")
			.master("local[*]")
			.config("spark.hadoop.fs.defaultFS", "hdfs://localhost:9000")
			.getOrCreate();

		Dataset<Row> workData = spark.createDataFrame(allWorks, WorkDto.class);
		workData = workData.na().fill("");
		Dataset<Row> filteredData = workData.filter(workData.col("subjects").notEqual(""));

		Tokenizer titleTokenizer = new Tokenizer().setInputCol("title").setOutputCol("titleTokens");
		Tokenizer descriptionTokenizer = new Tokenizer().setInputCol("description").setOutputCol("descriptionTokens");
		Tokenizer authorsTokenizer = new Tokenizer().setInputCol("authors").setOutputCol("authorsTokens");
		Tokenizer genresTokenizer = new Tokenizer().setInputCol("subjects").setOutputCol("subjectsTokens");

		StopWordsRemover titleStopWordsRemover = new StopWordsRemover().setInputCol("titleTokens").setOutputCol("titleWords");
		StopWordsRemover descriptionStopWordsRemover = new StopWordsRemover().setInputCol("descriptionTokens").setOutputCol("descriptionWords");
		StopWordsRemover authorsStopWordsRemover = new StopWordsRemover().setInputCol("authorsTokens").setOutputCol("authorsWords");
		StopWordsRemover subjectsStopWordsRemover = new StopWordsRemover().setInputCol("subjectsTokens").setOutputCol("subjectsWords");

		HashingTF titleHashingTF = new HashingTF().setInputCol("titleWords").setOutputCol("titleFeatures");
		HashingTF descriptionHashingTF = new HashingTF().setInputCol("descriptionWords").setOutputCol("descriptionFeatures");
		HashingTF authorsHashingTF = new HashingTF().setInputCol("authorsWords").setOutputCol("authorsFeatures");
		HashingTF genresHashingTF = new HashingTF().setInputCol("subjectsWords").setOutputCol("subjectsFeatures");

		VectorAssembler assembler = new VectorAssembler()
				.setInputCols(new String[]{"titleFeatures", "descriptionFeatures", "authorsFeatures", "subjectsFeatures"})
				.setOutputCol("features");

		PipelineStage[] pipelineStages = new PipelineStage[]{
				titleTokenizer,
				descriptionTokenizer,
				authorsTokenizer,
				genresTokenizer,
				titleStopWordsRemover,
				descriptionStopWordsRemover,
				authorsStopWordsRemover,
				subjectsStopWordsRemover,
				titleHashingTF,
				descriptionHashingTF,
				authorsHashingTF,
				genresHashingTF,
				assembler
		};
		Pipeline pipeline = new Pipeline().setStages(pipelineStages);

		PipelineModel pipelineModel = pipeline.fit(filteredData);
		Dataset<Row> transformedData = pipelineModel.transform(filteredData);

		System.out.println("ContentBasedRecommender: transformed data df:");
		transformedData.show(false);

		transformedData.createOrReplaceTempView("works");
		spark.udf().register("cosineSimilarity", (Vector v1, Vector v2) -> {
			double dotProduct = BLAS.dot(v1, v2);
			double norms = Vectors.norm(v1, 2) * Vectors.norm(v2, 2);
			return dotProduct / norms;
		}, DataTypes.DoubleType);

		Dataset<Row> df = spark.sql(
				"SELECT w1.id AS work1, w2.id AS work2, cosineSimilarity(w1.features, w2.features) AS similarity " +
						"FROM works w1 CROSS JOIN works w2 WHERE w1.id <> w2.id");
		df.show(false);

		Object[] ratedWorksArray = userRatedWorkIds.toArray();

		Dataset<Row> filteredSimilarities = df
			.filter(
				df.col("work1").isin(ratedWorksArray)
				.and(functions.not(df.col("work2").isin(ratedWorksArray)))
				.and(df.col("similarity").$greater$eq(SIMILARITY_THRESHOLD))
			);

		System.out.println("ContentBasedRecommender: dataframe with calculated similarities:");
		df.show(false);

		Dataset<Row> workRecommendations = filteredSimilarities.groupBy("work1")
			.agg(
				functions.collect_list("work2").alias("recommended_books"),
				functions.collect_list("similarity").alias("similarities")
			)
			.orderBy("work1");

		List<Row> rows = workRecommendations.collectAsList();

		List<RecommendationDto> recommendationsList = new ArrayList<>();
		for (Row row : rows) {
			scala.collection.mutable.ArraySeq<Object> recommendedWorksScalaSeq = row.getAs("recommended_books");
			List<Object> recommendedWorks = scala.jdk.CollectionConverters.SeqHasAsJava(recommendedWorksScalaSeq).asJava();

			scala.collection.mutable.ArraySeq<Object> similaritiesScalaSeq = row.getAs("similarities");
			List<Object> similarities = scala.jdk.CollectionConverters.SeqHasAsJava(similaritiesScalaSeq).asJava();


			for (int i = 0; i < recommendedWorks.size(); i++) {
				String workId = (String) recommendedWorks.get(i);
				double similarity = (double) similarities.get(i);

				RecommendationDto recommendation = new RecommendationDto();
				recommendation.setWorkId(workId);
				recommendation.setUserId(userId);
				recommendation.setScore(similarity);
				recommendationsList.add(recommendation);
			}
		}

		recommendationSaverService.saveBulk(RecommendationTable.CONTENT, recommendationsList);

		spark.stop();
	}
}
