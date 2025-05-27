package org.chumak.recommender.services.saver.common;

public enum RecommendationTable {
    COLLABORATIVE("cf_recommendations"),
    CONTENT("cb_recommendations");

    private final String value;

    RecommendationTable(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}