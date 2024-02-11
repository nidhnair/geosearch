package com.corelogic.li.geosearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.util.GeometryTransformer;
import org.locationtech.jts.util.GeometricShapeFactory;

import java.io.IOException;
import java.util.Random;

public class InsertClipGeometryIntoElasticsearch {

    private static final double RADIUS_LIMIT_FEET = 2000;
    private static final double METERS_TO_FEET = 3.28084;
    static double minDistance = RADIUS_LIMIT_FEET * 2;

    static ElasticsearchClient esClient;
    static ObjectMapper objectMapper;

    public static void main(String[] args) throws Exception {

        // Create ElasticsearchClient
        esClient = createElasticsearchClient();
        objectMapper = new ObjectMapper();

        try {

            // createIndex();
            // Index documents
            createGeometries();
        } finally {
            // Close ElasticsearchClient
            // esClient.close
        }
    }

    private static void createGeometries() throws JsonProcessingException, IOException {
        long millis = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            // Generate random com.corelogic.li.geosearch.ClipGeometry
            ClipGeometry clipGeometry = generateRandomClipGeometry();

            // Map com.corelogic.li.geosearch.ClipGeometry to JSON
//            String json = objectMapper.writeValueAsString(clipGeometry);
//
//            IndexRequest indexRequest = new IndexRequest("clshapes")
//                    .source(json, "application/json");

            IndexRequest<ClipGeometry> request = IndexRequest.of(x -> x
                    .index("products")
                    .id(clipGeometry.getClipId())
                    .document(clipGeometry)
            );

            IndexResponse response = esClient.index(request);

            System.out.println("Indexed with version " + response.version());

            // Introduce a delay to avoid flooding the server
            // Thread.sleep(100);
        }

        System.out.println("time per insert = " + (System.currentTimeMillis() - millis)/10 + " milli seconds");
    }

    // Create ElasticsearchClient
    private static ElasticsearchClient createElasticsearchClient() {

        String serverUrl = "http://localhost:9200";

        // Create the low-level client
        RestClient restClient = RestClient
                .builder(HttpHost.create(serverUrl))
                .build();

        // Create the transport with a Jackson mapper
        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper());

        // And create the API client
        return new ElasticsearchClient(transport);

    }

    // Method to generate random com.corelogic.li.geosearch.ClipGeometry
    private static ClipGeometry generateRandomClipGeometry() {
        Envelope usaBounds = new Envelope(-125, -65, 24, 49);

        // Generate random polygons
        Polygon polygon = generateRandomPolygon(usaBounds);

        // Generate random clipId
        String clipId = "clip_" + new Random().nextInt(1000);

        // Generate random Polygon geometry
        GeometryFactory geometryFactory = new GeometryFactory();

        return new ClipGeometry(clipId, polygon);
    }

    public static Polygon generateRandomPolygon(Envelope bounds) {

        Random random = new Random();

        // Generate a random point within the bounds
        double x = bounds.getMinX() + random.nextDouble() * (bounds.getMaxX() - bounds.getMinX());
        double y = bounds.getMinY() + random.nextDouble() * (bounds.getMaxY() - bounds.getMinY());

        while (true) {
            Point center = new GeometryFactory().createPoint(new Coordinate(x, y));

            // boolean farEnough = true;
            // for (Polygon existingPolygon : polygons) {
            // Point existingCenter = existingPolygon.getCentroid();
            // double distance = center.distance(existingCenter);
            // if (distance < minDistance) {
            // farEnough = false;
            // System.out.println("here");
            // break;
            // }
            // }

            // Generate random lengths for the rectangle sides
            double width = random.nextDouble() * RADIUS_LIMIT_FEET * 2;
            double height = random.nextDouble() * RADIUS_LIMIT_FEET * 2;

            // Create a rectangle around the point
            GeometricShapeFactory shapeFactory = new GeometricShapeFactory();
            shapeFactory.setCentre(center.getCoordinate());
            shapeFactory.setWidth(width);
            shapeFactory.setHeight(height);
            shapeFactory.setNumPoints(5);
            Polygon polygon = shapeFactory.createRectangle();

            // Convert the rectangle into a polygon
            // Polygon polygon = (Polygon) bufferGeometry(rectangle, Math.max(width, height)
            // / 2); // Buffer with half of
            // // the maximum side
            // // length

            // Check if the polygon is valid and has exactly 4 sides
            if (polygon.isValid()) {
                if (polygon.getNumInteriorRing() == 0) {
                    if (polygon.getExteriorRing().getNumPoints() == 5) {
                        System.out.println(polygon);
                        return polygon;
                    } else {
                        System.out.println("numpoints " + polygon.getExteriorRing().getNumPoints());
                    }
                } else {
                    System.out.println("interior ring");
                }

            } else {
                System.out.println("no valid");
            }
        }

    }

    // Method to buffer a geometry with a given distance (in meters)
    private static Geometry bufferGeometry(Geometry geometry, double distanceMeters) {
        GeometryTransformer transformer = new GeometryTransformer();
        // transformer.setCopyUserData(true);
        return transformer.transform(geometry.buffer(distanceMeters / METERS_TO_FEET));
    }

    public static void createIndex() throws Exception {
        // tag::create-products-index
        esClient.indices().create(c -> c
                .index("products"));
        // end::create-products-index
    }
}

class ClipGeometry {
    private String clipId;

    private String location;

    public ClipGeometry() {
    }

    public ClipGeometry(String clipId, Geometry polygon) {
        this.clipId = clipId;
        this.location = polygon.toText();
    }

    public String getClipId() {
        return clipId;
    }

    public void setClipId(String clipId) {
        this.clipId = clipId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(Geometry polygon) {

        this.location = polygon.toText();
    }
}
