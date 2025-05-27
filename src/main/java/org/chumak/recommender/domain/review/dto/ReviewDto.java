package org.chumak.recommender.domain.review.dto;

import lombok.Data;

@Data
public class ReviewDto {
	private String userId;
	private String workId;
	private Integer rating;
}
