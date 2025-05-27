package org.chumak.recommender.background_tasks;

import lombok.RequiredArgsConstructor;
import org.chumak.recommender.database.domain.cbr_requests.CbrRequest;
import org.chumak.recommender.domain.cbr_requests.CbrRequestsService;
import org.chumak.recommender.domain.user.UserService;
import org.chumak.recommender.services.recommenders.ContentBasedRecommender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ContentBasedRecommendationsTask {

    private final CbrRequestsService cbrRequestsService;
    private final ContentBasedRecommender contentBasedRecommender;
    private final UserService userService;

    @Value("${tasks.isContentBasedRecommenderTaskEnabled}")
    private boolean isContentBasedRecommenderTaskEnabled;

    private final Integer WORK_RATING_THRESHOLD = 1;

    @Scheduled(initialDelay = 10_000, fixedRate = 600_000)
    public void executeTask() {
        if (!isContentBasedRecommenderTaskEnabled) {
            return;
        }

        Optional<CbrRequest> cbrRequestOptional = cbrRequestsService.getRequestToExecute();

        if (!cbrRequestOptional.isPresent()) {
            System.out.println("ContentBasedRecommendationsTask: skipped. No requests found");
            return;
        }

        System.out.println("ContentBasedRecommendationsTask: started");
        CbrRequest cbrRequest = cbrRequestOptional.get();
        UUID userId = cbrRequest.getUser().getId();

        List<String> userRatedWorks = userService.getReviewedByUserWorkIds(userId, WORK_RATING_THRESHOLD);

        contentBasedRecommender.generateForUser(userId, userRatedWorks);

        cbrRequestsService.completeRequest(cbrRequest);
        System.out.println("ContentBasedRecommendationsTask: finished");
    }
}