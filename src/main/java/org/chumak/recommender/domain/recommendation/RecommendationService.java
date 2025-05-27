package org.chumak.recommender.domain.recommendation;

import lombok.RequiredArgsConstructor;
import org.chumak.recommender.database.domain.user.User;
import org.chumak.recommender.domain.user.UserService;
import org.chumak.recommender.domain.work.dto.WorkDto;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecommendationService {

	private final UserService userService;

	public ResponseEntity<?> getRecommendations(UUID userId) {
		Optional<User> userOptional = userService.findById(userId);

		if (!userOptional.isPresent()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
		}

		List<WorkDto> recommendedWorks = userService.findRecommendedWorksForUser(userId);

		return ResponseEntity.status(HttpStatus.OK).body(recommendedWorks);
	}
}
