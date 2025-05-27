package org.chumak.recommender.domain.work.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class WorkDto implements Serializable {
	private String id;
	private String title;
	private String description;
	private String authors;
	private String subjects;
}
