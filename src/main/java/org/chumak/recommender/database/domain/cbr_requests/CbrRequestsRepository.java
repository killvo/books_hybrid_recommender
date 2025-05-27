package org.chumak.recommender.database.domain.cbr_requests;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CbrRequestsRepository extends JpaRepository<CbrRequest, UUID> {

    Optional<CbrRequest> findFirstByCompletedAtIsNull();

    @Query("select r from CbrRequest r where r.user.id = :userId and r.completedAt is null")
    Optional<CbrRequest> findUncompletedRequestForUser(UUID userId);

}