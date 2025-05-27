package org.chumak.recommender.database.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    @Query(value = "select r.work_id from cb_recommendations r where r.user_id = :userId order by r.score desc limit :limit", nativeQuery = true)
    List<String> findContentBasedRecommendedWorkIdsForUser(UUID userId, Integer limit);

    @Query(value = "select r.work_id from cf_recommendations r where r.user_id = :userId order by r.score desc limit :limit", nativeQuery = true)
    List<String> findCollaborativeFilteringRecommendedWorkIdsForUser(UUID userId, Integer limit);
}