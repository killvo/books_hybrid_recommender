package org.chumak.recommender.domain.review;

import org.chumak.recommender.database.domain.review.Review;
import org.chumak.recommender.domain.review.dto.ReviewDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ReviewMapper {
    ReviewMapper MAPPER = Mappers.getMapper(ReviewMapper.class);

    @Mapping(source = "work.id", target = "workId")
    @Mapping(source = "user.id", target = "userId")
    ReviewDto toDto(Review review);
}
