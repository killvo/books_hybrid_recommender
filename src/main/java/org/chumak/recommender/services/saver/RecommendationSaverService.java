package org.chumak.recommender.services.saver;

import org.chumak.recommender.services.recommendation.dto.RecommendationDto;
import org.chumak.recommender.services.saver.common.RecommendationTable;
import org.jetbrains.annotations.NotNull;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Service
public class RecommendationSaverService {
    private final JdbcTemplate jdbcTemplate;

    public RecommendationSaverService(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void saveBulk(RecommendationTable recommendationType, List<RecommendationDto> recommendationDtos) {
        String tableName = recommendationType.getValue();

        String sql = "INSERT INTO " + tableName + " (user_id, work_id, score) VALUES (?, ?, ?) " +
                "ON CONFLICT (user_id, work_id) DO UPDATE SET score = EXCLUDED.score;";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(@NotNull PreparedStatement ps, int i) throws SQLException {
                RecommendationDto recommendationDto = recommendationDtos.get(i);

                ps.setObject(1, recommendationDto.getUserId());
                ps.setString(2, recommendationDto.getWorkId());
                ps.setDouble(3, recommendationDto.getScore());
            }

            @Override
            public int getBatchSize() {
                return recommendationDtos.size();
            }
        });
    }
}
