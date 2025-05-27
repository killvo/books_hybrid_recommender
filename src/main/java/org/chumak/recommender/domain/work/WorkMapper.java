package org.chumak.recommender.domain.work;

import org.chumak.recommender.database.domain.author.Author;
import org.chumak.recommender.database.domain.work.Work;
import org.chumak.recommender.domain.work.dto.WorkDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.stream.Collectors;

@Mapper
public interface WorkMapper {
    WorkMapper MAPPER = Mappers.getMapper(WorkMapper.class);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "authors", target = "authors", qualifiedByName = "mapAuthorsToString")
    @Mapping(source = "subjects", target = "subjects")
    WorkDto toDto(Work work);

    @Named("mapAuthorsToString")
    default String mapAuthorsToString(List<Author> authors) {
        if (authors == null) {
            return null;
        }

        return authors.stream()
                .map(Author::getFullName)
                .collect(Collectors.joining(","));
    }
}
