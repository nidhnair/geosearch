package com.corelogic.li.geosearch;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

public class WKTConverter {

    public static Geometry wktToPoint(String wktPoint) throws ParseException {
        WKTReader reader = new WKTReader();
        return reader.read(wktPoint);
    }
}
