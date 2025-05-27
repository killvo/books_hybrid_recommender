package org.chumak.recommender.database.domain.review;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    @Query(value = "SELECT r.* FROM reviews r LIMIT :limit", nativeQuery = true)
    List<Review> findLimited(Integer limit);

    @Query("SELECT distinct r.work.id FROM Review r WHERE r.user.id = :userId AND r.rating >= :rating")
    List<String> findWorkIdsByUserIdAndRating(UUID userId, Integer rating);

    List<Review> findAllByUserId(UUID userId);
}