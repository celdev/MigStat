package com.celdev.migstat.model.query;


/*  This is used by the parsers to determine if the query website is in english or swedish */
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
