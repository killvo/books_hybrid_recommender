package org.chumak.recommender.domain.user;

import lombok.RequiredArgsConstructor;
import org.chumak.recommender.database.domain.user.User;
import org.chumak.recommender.database.domain.user.UserRepository;
import org.chumak.recommender.domain.cbr_requests.CbrRequestsService;
import org.chumak.recommender.domain.review.ReviewService;
import org.chumak.recommender.domain.work.WorkService;
import org.chumak.recommender.domain.work.dto.WorkDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    private final CbrRequestsService cbrRequestsService;
    private final ReviewService reviewService;
    private final WorkService workService;

    @Value("${contentBasedRecsLimit}")
    private Integer CONTENT_BASED_RECS_LIMIT;

    @Value("${collaborativeFilteringRecsLimit}")
    private Integer COLLABORATIVE_FILTERING_RECS_LIMIT;

    public Optional<User> findById(UUID userId) {
        return userRepository.findById(userId);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public List<User> saveAll(List<User> users) {
        return userRepository.saveAll(users);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public List<WorkDto> findRecommendedWorksForUser(UUID userId) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (!userOptional.isPresent()) {
            return new ArrayList<>();
        }

        List<String> cbRecs = findContentBasedRecommendedWorkIdsForUser(userId);
        List<String> cfRecs = findCollaborativeFilteringRecommendedWorkIdsForUser(userId);

        if (cbRecs.isEmpty()) {
            createContentBasedRecsRequest(userOptional.get());
        }

        List<String> combinedRecs = new ArrayList<>();
        combinedRecs.addAll(cbRecs);
        combinedRecs.addAll(cfRecs);

        if (combinedRecs.isEmpty()) {
            return new ArrayList<>();
        }

        return workService.findWorksDtoByIds(combinedRecs);
    }

    private void createContentBasedRecsRequest(User user) {
        boolean isUserHaveEnoughReviewsForCbr = reviewService.checkIsUserHaveEnoughReviewsForCbr(user.getId());

        if (!isUserHaveEnoughReviewsForCbr) {
            return;
        }

        System.out.println("UserService: user " + user.getId() + " does not have content based recommendations. Request will be created.");

        cbrRequestsService.createForUser(user);
    }

    public List<String> findContentBasedRecommendedWorkIdsForUser(UUID userId) {
        return userRepository.findContentBasedRecommendedWorkIdsForUser(userId, CONTENT_BASED_RECS_LIMIT);
    }

    public List<String> findCollaborativeFilteringRecommendedWorkIdsForUser(UUID userId) {
        return userRepository.findCollaborativeFilteringRecommendedWorkIdsForUser(userId, COLLABORATIVE_FILTERING_RECS_LIMIT);
    }

    public List<String> getReviewedByUserWorkIds(UUID userId, Integer ratingThreshold) {
        return reviewService.getReviewedByUserWorkIds(userId, ratingThreshold);
    }

    public boolean checkIsUserHaveEnoughReviewsForCbr(UUID userId) {
        return reviewService.checkIsUserHaveEnoughReviewsForCbr(userId);
    }
}
