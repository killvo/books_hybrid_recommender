package org.chumak.recommender.services.recommenders;

import lombok.RequiredArgsConstructor;
import org.apache.spark.ml.recommendation.ALS;
import org.apache.spark.ml.recommendation.ALSModel;
import org.apache.spark.sql.*;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;
import org.chumak.recommender.domain.review.ReviewService;
import org.chumak.recommender.domain.review.dto.ReviewDto;
import org.chumak.recommender.services.recommendation.dto.RecommendationDto;
import org.chumak.recommender.services.saver.RecommendationSaverService;
import org.chumak.recommender.services.saver.common.RecommendationTable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CollaborativeFilteringRecommender {

	private final ReviewService reviewService;

	@Value("${collaborativeFilteringRecsLimit}")
	private Integer RECOMMENDATIONS_LIMIT;

	private final RecommendationSaverService recommendationSaverService;

	public void generateForAllUsers() {
		List<ReviewDto> reviews = reviewService.findAllDto();

		if (reviews.isEmpty()) {
			System.out.println("CollaborativeFilteringRecommender: There are no reviews to analyze.");
			return;
		}

		SparkSession spark = SparkSession.builder()
			.appName("CollaborativeFilteringRecommender")
			.master("local[*]")
			.config("spark.hadoop.fs.defaultFS", "hdfs://localhost:9000")
			.getOrCreate();

		Map<String, Integer> userIdMapping = new HashMap<>();
		Map<String, Integer> workIdMapping = new HashMap<>();
		Map<Integer, String> reverseUserIdMapping = new HashMap<>();
		Map<Integer, String> reverseWorkIdMapping = new HashMap<>();
		int userIdCounter = 0;
		int workIdCounter = 0;

		List<Row> rows = new ArrayList<>();

		for (ReviewDto review : reviews) {
			String userId = review.getUserId();
			String workId = review.getWorkId();
			int rating = review.getRating();

			Row row = RowFactory.create(userIdCounter, workIdCounter, rating);
			rows.add(row);

			if (!userIdMapping.containsKey(userId)) {
				userIdMapping.put(userId, userIdCounter);
				reverseUserIdMapping.put(userIdCounter, userId);
				userIdCounter++;
			}

			if (!workIdMapping.containsKey(workId)) {
				workIdMapping.put(workId, workIdCounter);
				reverseWorkIdMapping.put(workIdCounter, workId);
				workIdCounter++;
			}
		}

		StructType schema = new StructType()
				.add("userId", DataTypes.IntegerType)
				.add("workId", DataTypes.IntegerType)
				.add("rating", DataTypes.IntegerType);

		Dataset<Row> reviewDF = spark.createDataFrame(rows, schema);

		Dataset<Row> ratingsDF = reviewDF.select(
				functions.col("userId").cast(DataTypes.IntegerType),
				functions.col("workId").cast(DataTypes.IntegerType),
				functions.col("rating").cast(DataTypes.IntegerType)
		);

		ALS als = new ALS()
				.setMaxIter(5)
				.setRegParam(0.01)
				.setUserCol("userId")
				.setItemCol("workId")
				.setRatingCol("rating");
		ALSModel model = als.fit(ratingsDF);

		Dataset<Row> userRecs = model.recommendForAllUsers(RECOMMENDATIONS_LIMIT);

		List<Row> collectedRows = userRecs.collectAsList();

		List<RecommendationDto> recommendationsList = new ArrayList<>();

		for (Row row : collectedRows) {
			int userIdRaw = row.getInt(0);
			UUID userId = UUID.fromString(reverseUserIdMapping.get(userIdRaw));

			scala.collection.mutable.ArraySeq<Object> scalaSeq = row.getAs(1);
			List<Object> recommendedBooks = scala.jdk.CollectionConverters.SeqHasAsJava(scalaSeq).asJava();

			for (Object recommendedBook : recommendedBooks) {
				Row workRow = (Row) recommendedBook;
				int intId = workRow.getInt(0);
				float score = workRow.getFloat(1);
				String workId = reverseWorkIdMapping.get(intId);

				RecommendationDto recommendation = new RecommendationDto();
				recommendation.setUserId(userId);
				recommendation.setWorkId(workId);
				recommendation.setScore(score);

				recommendationsList.add(recommendation);
			}
		};

		recommendationSaverService.saveBulk(RecommendationTable.COLLABORATIVE, recommendationsList);

		spark.stop();
	}
}
