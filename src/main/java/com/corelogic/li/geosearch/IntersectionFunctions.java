package com.corelogic.li.geosearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.GeoShapeRelation;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
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

        String identifiedNeighborhoodLocation = searchNeighborhoodsByName(es, "Ridgewood");

        ElasticDao elasticDao = new ElasticDao(es);

        List<SomeShapesfromNYC> someShapesfromNYCS = findSubwayStationsInNeigborHood(es, identifiedNeighborhoodLocation);

        elasticDao.createSomeShapeGeometries("newshapes", someShapesfromNYCS);
//
//        for (SomeShapesfromNYC someShapesfromNYC : nearestNeighborShapes) {
//            System.out.println(someShapesfromNYC.getAttributeMap().get("LONG_NAME"));
//        }

//      List<SomeShapesfromNYC> testellationList =  createTessellation(nearestNeighborShapes);
//
//        elasticDao.createSomeShapeGeometries("newshapes", testellationList);
//


//        elasticDao.createSomeShapeGeometries("newshapes", triangulationList);

    }

    private static List<SomeShapesfromNYC> findSubwayStationsInNeigborHood(ElasticsearchClient esClient, String identifiedNeighborhoodLocation) throws IOException {

        SearchResponse<SomeShapesfromNYC> response = esClient.search(s -> {
            return s.index("nycsubwaystations").
                    query(q -> q.geoShape(v -> v.field("location").shape(shp -> shp.shape(JsonData.of(identifiedNeighborhoodLocation)).
                            relation(GeoShapeRelation.Within))))
                    .size(500);
        }, SomeShapesfromNYC.class);


        List<Hit<SomeShapesfromNYC>> hits = response.hits().hits();
        System.out.println("NEARBY Stations ");
        System.out.println("_____________________");
        System.out.println("_____________________");
        System.out.println(response.hits().total().toString());

        List<SomeShapesfromNYC> someShapesfromNYCS = new ArrayList<>();

        for (Hit<SomeShapesfromNYC> hit : hits) {
            SomeShapesfromNYC someShapesfromNYC = hit.source();
            System.out.println("Name: " + someShapesfromNYC.getAttributeMap().get("NAME") +
                    " Location " + someShapesfromNYC.getLocation());
            someShapesfromNYCS.add(someShapesfromNYC);
        }
        return someShapesfromNYCS;
    }


    public static String searchNeighborhoodsByName(ElasticsearchClient esClient, String neighborhoodName) throws IOException {

        SearchResponse<SomeShapesfromNYC> response = esClient.search(s -> s
                .index("nycneighborhoods")
                .query(q -> {
                            return q
                                    .match(t -> t.lenient(false)
                                            .field("attributeMap.NAME.keyword")
                                            .query(neighborhoodName)
                                    );
                        }
                ), SomeShapesfromNYC.class);


        List<Hit<SomeShapesfromNYC>> hits = response.hits().hits();

        for (Hit<SomeShapesfromNYC> hit : hits) {
            SomeShapesfromNYC someShapesfromNYC = hit.source();
            System.out.println("Name: " + someShapesfromNYC.getAttributeMap().get("NAME") +
                    " Location " + someShapesfromNYC.getLocation());
            return someShapesfromNYC.getLocation();
        }

        return null;

    }

}
