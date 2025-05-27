package org.chumak.recommender.services.seed;

import com.github.javafaker.Faker;
import org.chumak.recommender.database.domain.user.User;
import org.chumak.recommender.database.domain.work.Work;
import org.chumak.recommender.domain.user.UserService;
import org.chumak.recommender.domain.work.WorkService;
import org.jetbrains.annotations.NotNull;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
public class SeedService {

    private final UserService userService;
    private final WorkService workService;
    private final Faker faker;

    private final DataSource dataSource;

    public SeedService(UserService userService, WorkService workService, DataSource dataSource) {
        this.userService = userService;
        this.workService = workService;
        this.dataSource = dataSource;
        this.faker = new Faker();
    }

    private final Integer USERS_AMOUNT = 200_000;
    private final Integer MIN_REVIEWS_PER_WORK_AMOUNT = 5;
    private final Integer MAX_REVIEWS_PER_WORK_AMOUNT = 25;
    private final Integer MIN_RATING_VALUE = 1;
    private final Integer MAX_RATING_VALUE = 5;

    public void seedDatabase() {
        seedUsers();
        seedReviews();
    }

    private void seedUsers() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        String sql = "INSERT INTO users (id, email, full_name, password, created_at) VALUES (?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());

            @Override
            public void setValues(@NotNull PreparedStatement ps, int i) throws SQLException {
                ps.setObject(1, UUID.randomUUID());
                ps.setString(2, faker.internet().emailAddress());
                ps.setString(3, faker.name().fullName());
                ps.setString(4, faker.internet().password());
                ps.setTimestamp(5, timestamp);
            }

            @Override
            public int getBatchSize() {
                return USERS_AMOUNT;
            }
        });
    }

    public void seedReviews() {
        List<User> users = userService.findAll();
        List<Work> works = workService.findAll();
        Random random = new Random();

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        String sql = "INSERT INTO reviews (id, work_id, user_id, created_at, rating) VALUES (?, ?, ?, ?, ?)";

        for (int j = 0; j < works.size(); j++) {
            int reviewCount = MIN_REVIEWS_PER_WORK_AMOUNT + random.nextInt(MAX_REVIEWS_PER_WORK_AMOUNT + 1);

            int finalJ = j;
            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(@NotNull PreparedStatement ps, int i) throws SQLException {
                    User user = users.get(random.nextInt(users.size()));
                    int rating = random.nextInt(MAX_RATING_VALUE) + MIN_RATING_VALUE;
                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());

                    ps.setObject(1, UUID.randomUUID());
                    ps.setString(2, works.get(finalJ).getId());
                    ps.setObject(3, user.getId());
                    ps.setTimestamp(4, timestamp);
                    ps.setInt(5, rating);
                }

                @Override
                public int getBatchSize() {
                    return reviewCount;
                }
            });
        }
    }
}
