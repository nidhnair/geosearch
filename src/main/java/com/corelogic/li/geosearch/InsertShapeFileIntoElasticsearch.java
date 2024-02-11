package com.corelogic.li.geosearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.geotools.api.data.DataStore;
import org.geotools.api.data.DataStoreFinder;
import org.geotools.api.data.FeatureSource;
import org.geotools.api.feature.Property;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.filter.Filter;
import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.api.referencing.operation.TransformException;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Geometry;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InsertShapeFileIntoElasticsearch {

    private static final double RADIUS_LIMIT_FEET = 2000;
    private static final double METERS_TO_FEET = 3.28084;
    static double minDistance = RADIUS_LIMIT_FEET * 2;

    static ElasticsearchClient esClient;
    static ObjectMapper objectMapper;

    public static void main(String[] args) throws Exception {

        String shapefilePath = "/Users/nnair/Documents/geospatial/postgis-workshop/data/nyc_neighborhoods.shp";


        List<SomeShapesfromNYC> someShapesfromNYCS = readSomeShapeFileFromNYC(shapefilePath);

        // Create ElasticsearchClient
        esClient = ElasticSearchClientCreator.createElasticsearchClient();
        objectMapper = new ObjectMapper();

        try {

            // createIndex();
            // Index documents
//            createGeometries(nycCityGeometries);'
            ElasticDao elasticDao = new ElasticDao(esClient);
            elasticDao.createSomeShapeGeometries("nycneighborhoods", someShapesfromNYCS);
        } finally {
            // Close ElasticsearchClient
//             esClient.clo()
        }
    }


    public static List<SomeShapesfromNYC> readSomeShapeFileFromNYC(String shapefilePath) throws IOException, FactoryException, TransformException {
        List<SomeShapesfromNYC> someShapesfromNYCS = new ArrayList<>();

        // Read shapefile
        File shapefile = new File(shapefilePath);
        Map<String, Object> map = new HashMap<>();
        map.put("url", shapefile.toURI().toURL());


        DataStore dataStore = DataStoreFinder.getDataStore(map);
        String typeName = dataStore.getTypeNames()[0];

        FeatureSource<SimpleFeatureType, SimpleFeature> source =
                dataStore.getFeatureSource(typeName);
        Filter filter = Filter.INCLUDE; // ECQL.toFilter("BBOX(THE_GEOM, 10,20,30,40)")

        String wkt = "PROJCS[\"NAD83 / UTM zone 18N\",GEOGCS[\"NAD83\",DATUM[\"North_American_Datum_1983\",SPHEROID[\"GRS 1980\",6378137,298.257222101,AUTHORITY[\"EPSG\",\"7019\"]],AUTHORITY[\"EPSG\",\"6269\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.01745329251994328,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4269\"]],UNIT[\"metre\",1,AUTHORITY[\"EPSG\",\"9001\"]],PROJECTION[\"Transverse_Mercator\"],PARAMETER[\"latitude_of_origin\",0],PARAMETER[\"central_meridian\",-75],PARAMETER[\"scale_factor\",0.9996],PARAMETER[\"false_easting\",500000],PARAMETER[\"false_northing\",0],AUTHORITY[\"EPSG\",\"26918\"],AXIS[\"Easting\",EAST],AXIS[\"Northing\",NORTH]]";

        String wkt4326 = "GEOGCS[\"WGS 84\",\n" +
                "    DATUM[\"WGS_1984\",\n" +
                "        SPHEROID[\"WGS 84\",6378137,298.257223563,\n" +
                "            AUTHORITY[\"EPSG\",\"7030\"]],\n" +
                "        AUTHORITY[\"EPSG\",\"6326\"]],\n" +
                "    PRIMEM[\"Greenwich\",0,\n" +
                "        AUTHORITY[\"EPSG\",\"8901\"]],\n" +
                "    UNIT[\"degree\",0.0174532925199433,\n" +
                "        AUTHORITY[\"EPSG\",\"9122\"]],\n" +
                "    AUTHORITY[\"EPSG\",\"4326\"]]";
        CoordinateReferenceSystem sourceCRS = CRS.parseWKT(wkt);
        CoordinateReferenceSystem targetCRS = CRS.parseWKT(wkt4326);


        MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS, true);

        FeatureCollection<SimpleFeatureType, SimpleFeature> collection = source.getFeatures(filter);
        try (FeatureIterator<SimpleFeature> features = collection.features()) {
            while (features.hasNext()) {
                SimpleFeature feature = features.next();
                System.out.print(feature.getID());
                System.out.print(": ");
                System.out.println(feature.getDefaultGeometryProperty().getValue());
                SomeShapesfromNYC someShapesfromNYC = new SomeShapesfromNYC();
                someShapesfromNYC.setId(feature.getID());
                Geometry sourceGeometry = (Geometry) feature.getDefaultGeometryProperty().getValue();

                Geometry targetGeometry = JTS.transform(sourceGeometry, transform);
                someShapesfromNYC.setLocation(targetGeometry.toText());

                Map<String, String> attributeMap = new HashMap<>();

                for (Property attribute : feature.getProperties()) {
                    if (attribute.getType().getName().getLocalPart().equals("String")) {

                        System.out.println("\t" + attribute.getName().getLocalPart() + ":" + attribute.getValue());
                        attributeMap.put(attribute.getName().getLocalPart(), (String) attribute.getValue());
                    }
                }

                someShapesfromNYC.setAttributeMap(attributeMap);

                someShapesfromNYCS.add(someShapesfromNYC);
            }
        }


        dataStore.dispose(); // Release resources

        return someShapesfromNYCS;
    }

}
