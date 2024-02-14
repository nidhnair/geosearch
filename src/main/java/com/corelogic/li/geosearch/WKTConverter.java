package com.corelogic.li.geosearch;

import com.google.common.geometry.*;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import java.util.ArrayList;
import java.util.List;

public class WKTConverter {

    public static String convertS2LatLngRectToWKT(S2LatLngRect rect) {
        StringBuilder wktBuilder = new StringBuilder();
        wktBuilder.append("POLYGON((");

        // Append the coordinates of the rectangle corners
        appendCoordinate(wktBuilder, rect.latLo().degrees(), rect.lngLo().degrees());
        wktBuilder.append(",");
        appendCoordinate(wktBuilder, rect.latHi().degrees(), rect.lngLo().degrees());
        wktBuilder.append(",");
        appendCoordinate(wktBuilder, rect.latHi().degrees(), rect.lngHi().degrees());
        wktBuilder.append(",");
        appendCoordinate(wktBuilder, rect.latLo().degrees(), rect.lngHi().degrees());
        wktBuilder.append(",");
        appendCoordinate(wktBuilder, rect.latLo().degrees(), rect.lngLo().degrees());

        // Close the polygon
        wktBuilder.append("))");

        return wktBuilder.toString();
    }

    private static void appendCoordinate(StringBuilder builder, double lat, double lng) {
        builder.append(lng).append(" ").append(lat);
    }
    public static Geometry wktToPoint(String wktPoint) throws ParseException {
        WKTReader reader = new WKTReader();
        return reader.read(wktPoint);
    }


    public static S2Region convertWKTToS2Region(String wkt) {
        Geometry geom;
        try {
            // Parse the WKT string into a JTS Geometry object
            WKTReader reader = new WKTReader();
            geom = reader.read(wkt);
        } catch (ParseException e) {
            System.err.println("Error parsing WKT string: " + e.getMessage());
            return null;
        }

        // Convert the JTS Geometry object into an S2Region
        return jtsToS2Region(geom);
    }

    private static S2Region jtsToS2Region(Geometry geom) {
        if (geom instanceof Point) {
            Point point = (Point) geom;
            S2LatLng latLng = S2LatLng.fromDegrees(point.getY(), point.getX());
            return new S2PointRegion(latLng.toPoint());
        } else if (geom instanceof Polygon) {
            Polygon polygon = (Polygon) geom;
            return jtsPolygonToS2Region(polygon);
        } else if (geom instanceof MultiPolygon) {
            MultiPolygon multiPolygon = (MultiPolygon) geom;

            for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
                List<S2Loop> loops = new ArrayList<>();
                Geometry geometry = multiPolygon.getGeometryN(i);
                if (geometry instanceof Polygon) {
                    Polygon polygon = (Polygon)geometry;
                   return jtsPolygonToS2Region(polygon);

//                    return  jtsPolygonToS2Region((Polygon) geometry);
                }
            }
            return null;
        } else {
            System.err.println("Unsupported geometry type: " + geom.getGeometryType());
            return null;
        }
    }

    private static S2Region jtsPolygonToS2Region(Polygon polygon) {
        S2PolygonBuilder.Options options =  S2PolygonBuilder.Options.builder()
                .setUndirectedEdges(false)
                .setXorEdges(true)
                .build();
        S2PolygonBuilder polygonBuilder = new S2PolygonBuilder(options);
//        List<S2Loop> loops = new ArrayList<>();
//        loops.add(jtsPolygonToS2Loop(polygon.getExteriorRing()));
        S2Loop loop = jtsPolygonToS2Loop(polygon.getExteriorRing());
        loop.normalize();
        polygonBuilder.addLoop(loop);

//        for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
//            loops.add(jtsPolygonToS2Loop(polygon.getInteriorRingN(i)));
//        }
        S2Polygon s2Polygon = polygonBuilder.assemblePolygon();
        return s2Polygon;
    }

    private static S2Loop jtsPolygonToS2Loop(LinearRing ring) {
        List<S2Point> points = new ArrayList<>();
        for (Coordinate coord : ring.getCoordinates()) {
            points.add(S2LatLng.fromDegrees(coord.y, coord.x).toPoint());
        }
        return new S2Loop(points);
    }

    public static void main(String[] args) {
        String wkt = "POLYGON((-122.358 47.653, -122.348 47.649, -122.348 47.659, -122.358 47.653))";
        S2Region s2Region = convertWKTToS2Region(wkt);
        if (s2Region != null) {
            System.out.println("Successfully converted WKT to S2Region: " + s2Region);
        } else {
            System.out.println("Failed to convert WKT to S2Region.");
        }
    }
}
