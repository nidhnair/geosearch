package com.corelogic.li.geosearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.GeoDistanceType;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import org.geotools.api.referencing.FactoryException;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.index.strtree.STRtree;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.triangulate.DelaunayTriangulationBuilder;
import org.locationtech.jts.triangulate.VoronoiDiagramBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SubwayFunctions {

    //search and knn for subwways
    //triangultion

    public static final String SUBWAY_INDEX_NAME = "nycsubwaystations";

    public static void main(String[] args) throws IOException, FactoryException, ParseException {
        ElasticsearchClient es = ElasticSearchClientCreator.createElasticsearchClient();

        String identifiedStationLocation = searchSubwayStationByName(es);

         List<SomeShapesfromNYC> someShapesfromNYCS =  findNearbySubwayStations(es, identifiedStationLocation);

        int numnearest = 10;
     List<SomeShapesfromNYC> nearestNeighborShapes = findNearestNeighbors(identifiedStationLocation, someShapesfromNYCS, numnearest);

        System.out.println( numnearest +" nearest are: ");

        ElasticDao elasticDao = new ElasticDao(es);

        elasticDao.createSomeShapeGeometries("newshapes", nearestNeighborShapes);

        for (SomeShapesfromNYC someShapesfromNYC : someShapesfromNYCS) {
            System.out.println(someShapesfromNYC.getAttributeMap().get("LONG_NAME"));
        }

//      List<SomeShapesfromNYC> testellationList =  createTessellation(nearestNeighborShapes);

//        elasticDao.createSomeShapeGeometries("newshapes", testellationList);

        List<SomeShapesfromNYC> triangulationList =  createDelaunayTriangulation(nearestNeighborShapes);

        elasticDao.createSomeShapeGeometries("newshapes", triangulationList);

    }

    public static List<SomeShapesfromNYC> createTessellation(List<SomeShapesfromNYC> someShapesfromNYCList) {
        List<Coordinate> points = new ArrayList<>() ;


        for (SomeShapesfromNYC someShapesfromNYC : someShapesfromNYCList) {
            try {
                Geometry g = WKTConverter.wktToPoint(someShapesfromNYC.getLocation());
                points.add(g.getCoordinate());
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

        }
        List<SomeShapesfromNYC> testellationList = new ArrayList<>();

        VoronoiDiagramBuilder voronoiBuilder = new VoronoiDiagramBuilder();
        voronoiBuilder.setSites(points);
        Geometry diagram = voronoiBuilder.getDiagram(new GeometryFactory());
        for (int i=0;i<diagram.getNumGeometries();i++){
            SomeShapesfromNYC someShapesfromNYC = new SomeShapesfromNYC();
            Geometry geometryN = diagram.getGeometryN(i);
            someShapesfromNYC.setLocation(geometryN.toText());
            someShapesfromNYC.setId("testellation"+i);
            testellationList.add(someShapesfromNYC);
        }

        return testellationList;
    }

    public static List<SomeShapesfromNYC> createDelaunayTriangulation(List<SomeShapesfromNYC> someShapesfromNYCList) {
        List<Coordinate> points = new ArrayList<>() ;


        for (SomeShapesfromNYC someShapesfromNYC : someShapesfromNYCList) {
            try {
                Geometry g = WKTConverter.wktToPoint(someShapesfromNYC.getLocation());
                points.add(g.getCoordinate());
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

        }
        List<SomeShapesfromNYC> testellationList = new ArrayList<>();

        DelaunayTriangulationBuilder builder = new DelaunayTriangulationBuilder();
        builder.setSites(points);
        Geometry diagram = builder.getTriangles(new GeometryFactory());
        for (int i=0;i<diagram.getNumGeometries();i++){
            SomeShapesfromNYC someShapesfromNYC = new SomeShapesfromNYC();
            Geometry geometryN = diagram.getGeometryN(i);
            someShapesfromNYC.setLocation(geometryN.toText());
            someShapesfromNYC.setId("triangulation"+i);
            testellationList.add(someShapesfromNYC);
        }

        return testellationList;
    }

    public static String searchSubwayStationByName(ElasticsearchClient esClient) throws IOException {

        SearchResponse<SomeShapesfromNYC> response = esClient.search(s -> s
                .index("nycsubwaystations")
                .query(q -> {
                            return q
                                    .match(t -> t.lenient(false)
                                            .field("attributeMap.NAME.keyword")
                                            .query("York St")
                                    );
                        }
                ), SomeShapesfromNYC.class);


        List<Hit<SomeShapesfromNYC>> hits = response.hits().hits();

        for (Hit<SomeShapesfromNYC> hit : hits) {
            SomeShapesfromNYC someShapesfromNYC = hit.source();
            System.out.println("Name: " + someShapesfromNYC.getAttributeMap().get("NAME") +
                    " Location " + someShapesfromNYC.getLocation() + " Precision " + hit.score().toString());
            return someShapesfromNYC.getLocation();
        }

        return null;

    }

    public static List<SomeShapesfromNYC> findNearbySubwayStations(ElasticsearchClient esClient, String identifiedStationLocation) throws IOException {

        SearchResponse<SomeShapesfromNYC> response = esClient.search(s -> s
                .index("nycsubwaystations")
                .query(query -> {
                            return query.geoDistance(geoDistanceQuery -> geoDistanceQuery.
                                    field("location").
                                    distance("500km").
                                    location(geolocation -> geolocation.text(identifiedStationLocation)).
                                    distanceType(GeoDistanceType.Arc)
                            );
                        }
                ).size(500), SomeShapesfromNYC.class);


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

    public static List<SomeShapesfromNYC> findNearestNeighbors(String identifiedStationLocation, List<SomeShapesfromNYC> someShapesfromNYCS, int k) throws FactoryException, ParseException {

        STRtree stRtree = new STRtree();

        Geometry searchLocation = WKTConverter.wktToPoint(identifiedStationLocation);

        for (SomeShapesfromNYC someShapesfromNYC : someShapesfromNYCS) {

            try {
                Geometry g = WKTConverter.wktToPoint(someShapesfromNYC.getLocation());
                NYCGeometryWrapper nycGeometryWrapper = new NYCGeometryWrapper(g,someShapesfromNYC);
                stRtree.insert(g.getEnvelopeInternal(),nycGeometryWrapper);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

        }


        Object[] nearestNeighbors = stRtree.nearestNeighbour(searchLocation.getEnvelopeInternal(), searchLocation, new NycItemDistanceImpl(), k);


        List<SomeShapesfromNYC> result = new ArrayList<>();
        for (Object obj : nearestNeighbors) {
            if (obj instanceof NYCGeometryWrapper) {
                result.add(((NYCGeometryWrapper) obj).getSomeShapesfromNYC());
            }
        }

        return result;
    }



}
