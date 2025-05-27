package org.chumak.recommender.services.recommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecommendationDto {
    private UUID userId;
    private String workId;
    private double score;
}
