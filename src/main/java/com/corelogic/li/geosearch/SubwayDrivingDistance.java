package com.corelogic.li.geosearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.GeoDistanceType;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.geotools.api.referencing.FactoryException;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.index.strtree.STRtree;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.locationtech.jts.triangulate.DelaunayTriangulationBuilder;
import org.locationtech.jts.triangulate.VoronoiDiagramBuilder;

import java.io.IOException;
import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.List;

public class SubwayDrivingDistance {

    //search and knn for subwways
    //triangultion

    public static final String SUBWAY_INDEX_NAME = "nycsubwaystations";

    public static void main(String[] args) throws IOException, FactoryException, ParseException {
        ElasticsearchClient es = ElasticSearchClientCreator.createElasticsearchClient();

        String identifiedStationLocation = searchSubwayStationByName(es);

        Point point = (Point) WKTConverter.wktToPoint(identifiedStationLocation);
        double longitude = point.getX();
        double latitude = point.getY();

        System.out.println("identifiedStationLocation = " + identifiedStationLocation);
        System.out.println("longitude = " + longitude + " latitude= " + latitude);

        String responseBody = callIsochroneAPI(latitude, longitude, 600);

        ElasticDao elasticDao = new ElasticDao(es);
        System.out.println(responseBody);

        SomeShapesfromNYC someShapesfromNYC = new SomeShapesfromNYC();
        someShapesfromNYC.setId("iso");
        String geoJson = extractGeometry(responseBody);


        Geometry geometry = new GeoJsonReader().read(geoJson);

        Geometry bound = geometry.getBoundary();


            WKTWriter wktWriter = new WKTWriter();
           String wkt =  wktWriter.write(geometry);



        someShapesfromNYC.setLocation(wkt);
        System.out.println("geoJson = " + wkt);
        List<SomeShapesfromNYC> someShapesfromNYCS = new ArrayList<>();
        someShapesfromNYCS.add(someShapesfromNYC);



        elasticDao.createSomeShapeGeometries("tempshapes", someShapesfromNYCS);


//        elasticDao.createSomeShapeGeometries("newshapes", nearestNeighborShapes);

//        for (SomeShapesfromNYC someShapesfromNYC : someShapesfromNYCS) {
//            System.out.println(someShapesfromNYC.getAttributeMap().get("LONG_NAME"));
//        }

//      List<SomeShapesfromNYC> testellationList =  createTessellation(nearestNeighborShapes);
//
//        elasticDao.createSomeShapeGeometries("newshapes", testellationList);

//        List<SomeShapesfromNYC> triangulationList =  createDelaunayTriangulation(nearestNeighborShapes);
//
//        elasticDao.createSomeShapeGeometries("newshapes", triangulationList);

    }


    public static String extractGeometry(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);
        JsonNode polygonsNode = root.path("polygons");
        if (!polygonsNode.isArray() || polygonsNode.size() == 0) {
            throw new IllegalArgumentException("No 'polygons' array found in JSON.");
        }

        JsonNode firstPolygonNode = polygonsNode.get(0);
        JsonNode geometryNode = firstPolygonNode.path("geometry");
         String geometryString = mapper.writeValueAsString(geometryNode);
        System.out.println("geometryString = " + geometryString);
       return geometryString;
    }

    public static String callIsochroneAPI(double latitude, double longitude, int timeInSeconds) throws IOException {
        String apiKey = "your_api_key"; // Replace with your GraphHopper API key
        String apiUrl = "http://localhost:8989/isochrone";

        // Build the URL for the Isochrone API request
        String url = String.format("%s?point=%.6f,%.6f&time_limit=%d&profile=car&type=json",
                apiUrl, latitude, longitude, timeInSeconds);

        System.out.println("url = " + url);

        // Create an HTTP client
        CloseableHttpClient httpClient = HttpClients.createDefault();

        // Create an HTTP GET request
        HttpGet request = new HttpGet(url);

        // Send the GET request and get the response
        HttpResponse response = httpClient.execute(request);

        // Read the response body as a string
        String responseBody = EntityUtils.toString(response.getEntity());

        // Return the response body
        return responseBody;
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


}
