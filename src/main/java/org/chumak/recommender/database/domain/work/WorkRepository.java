package org.chumak.recommender.database.domain.work;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkRepository extends JpaRepository<Work, String> {

    List<Work> findAllByIdIn(List<String> ids);

    @Query(value = "SELECT w.* FROM works w LIMIT :limit", nativeQuery = true)
    List<Work> findLimited(Integer limit);
}
