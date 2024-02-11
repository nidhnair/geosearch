package com.corelogic.li.geosearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import org.geotools.api.referencing.FactoryException;
import org.locationtech.jts.io.ParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class IntersectionFunctions {

    //search and knn for subwways
    //triangultion

    public static final String SUBWAY_INDEX_NAME = "nycsubwaystations";

    public static void main(String[] args) throws IOException, FactoryException, ParseException {
        ElasticsearchClient es = ElasticSearchClientCreator.createElasticsearchClient();

        List<SomeShapesfromNYC> someShapesfromNYCS = findAllTracts(es);




    }

    public static List<SomeShapesfromNYC> findAllTracts(ElasticsearchClient esClient) throws IOException {

        SearchResponse<SomeShapesfromNYC> response = esClient.search(s -> s
                .index("tl_2023_48_tract")
                .query(q -> q.matchAll(v -> v.queryName(""))
                ), SomeShapesfromNYC.class);


        List<Hit<SomeShapesfromNYC>> hits = response.hits().hits();

        List<SomeShapesfromNYC> someShapesfromNYCS = new ArrayList<>();

        for (Hit<SomeShapesfromNYC> hit : hits) {
            SomeShapesfromNYC someShapesfromNYC = hit.source();
            System.out.println("Name: " + someShapesfromNYC.getAttributeMap().get("NAME") +
                    " Location " + someShapesfromNYC.getLocation());


        }

        return someShapesfromNYCS;

    }

}
