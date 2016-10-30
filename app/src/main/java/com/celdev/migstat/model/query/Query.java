package com.celdev.migstat.model.query;

public class Query {

    public enum SwedishOrEnglishQuery{
        SWEDISH,
        ENGLISH
    }

    private String query;
    private SwedishOrEnglishQuery swedishOrEnglishQuery;

    public Query(String query, SwedishOrEnglishQuery swedishOrEnglishQuery) {
        this.query = query;
        this.swedishOrEnglishQuery = swedishOrEnglishQuery;
    }


}
