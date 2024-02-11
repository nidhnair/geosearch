package com.corelogic.li.geosearch;

import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.referencing.CRS;

public class CRSGenerator {
    static CoordinateReferenceSystem  getNad83() throws FactoryException {
        String wkt = "PROJCS[\"NAD83 / UTM zone 18N\",GEOGCS[\"NAD83\",DATUM[\"North_American_Datum_1983\",SPHEROID[\"GRS 1980\",6378137,298.257222101,AUTHORITY[\"EPSG\",\"7019\"]],AUTHORITY[\"EPSG\",\"6269\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.01745329251994328,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4269\"]],UNIT[\"metre\",1,AUTHORITY[\"EPSG\",\"9001\"]],PROJECTION[\"Transverse_Mercator\"],PARAMETER[\"latitude_of_origin\",0],PARAMETER[\"central_meridian\",-75],PARAMETER[\"scale_factor\",0.9996],PARAMETER[\"false_easting\",500000],PARAMETER[\"false_northing\",0],AUTHORITY[\"EPSG\",\"26918\"],AXIS[\"Easting\",EAST],AXIS[\"Northing\",NORTH]]";


        CoordinateReferenceSystem sourceCRS = CRS.parseWKT(wkt);

        return sourceCRS;
    }

    public static CoordinateReferenceSystem getwgs84CRS() throws FactoryException {
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
        CoordinateReferenceSystem targetCRS = CRS.parseWKT(wkt4326);
        return targetCRS;
    }
}