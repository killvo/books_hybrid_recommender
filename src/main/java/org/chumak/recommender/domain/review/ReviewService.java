package org.chumak.recommender.domain.review;

import lombok.RequiredArgsConstructor;
import org.chumak.recommender.database.domain.review.Review;
import org.chumak.recommender.database.domain.review.ReviewRepository;
import org.chumak.recommender.domain.review.dto.ReviewDto;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;

    @Value("${user.reviews.minAmountForCbr}")
    private Integer MIN_USER_REVIEWS_AMOUNT_FOR_CBR;

    public Review save(Review review) {
        return reviewRepository.save(review);
    }

    public List<Review> saveAll(List<Review> reviews) {
        return reviewRepository.saveAll(reviews);
    }

    public Optional<Review> findById(UUID id) {
        return reviewRepository.findById(id);
    }

    public List<ReviewDto> findAllDto() {
        return reviewRepository.findAll().stream()
                .map(ReviewMapper.MAPPER::toDto)
                .collect(Collectors.toList());
    }

    public List<Review> findAll() {
        return reviewRepository.findAll();
    }

    public List<String> getReviewedByUserWorkIds(UUID userId, Integer ratingThreshold) {
        return reviewRepository.findWorkIdsByUserIdAndRating(userId, ratingThreshold);
    }

    public boolean checkIsUserHaveEnoughReviewsForCbr(UUID userId) {
        List<Review> reviews = reviewRepository.findAllByUserId(userId);

        return reviews.size() >= MIN_USER_REVIEWS_AMOUNT_FOR_CBR;
    }
}
