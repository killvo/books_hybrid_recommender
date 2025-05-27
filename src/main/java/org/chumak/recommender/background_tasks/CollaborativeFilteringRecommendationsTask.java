package org.chumak.recommender.background_tasks;

import lombok.RequiredArgsConstructor;
import org.chumak.recommender.services.recommenders.CollaborativeFilteringRecommender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CollaborativeFilteringRecommendationsTask {

    private final CollaborativeFilteringRecommender collaborativeFilteringRecommender;

    @Value("${tasks.isCollaborativeFilteringRecommenderTaskEnabled}")
    private boolean isCollaborativeFilteringRecommenderTaskEnabled;

    @Scheduled(initialDelay = 10_0000, fixedRate = 600_000)
    public void executeTask() {
        if (!isCollaborativeFilteringRecommenderTaskEnabled) {
            return;
        }

        System.out.println("CollaborativeFilteringRecommendationsTask: started");

        collaborativeFilteringRecommender.generateForAllUsers();

        System.out.println("CollaborativeFilteringRecommendationsTask: finished");
    }
}