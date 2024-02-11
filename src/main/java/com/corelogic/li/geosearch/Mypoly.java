package com.corelogic.li.geosearch;

import org.locationtech.jts.geom.Geometry;


public class Mypoly {

    int id;
    String geometry;

    public Mypoly(Geometry polygon) {
       geometry = polygon.toText();
       id = polygon.toText().hashCode();
    }

    public int getId(){
        return id;
    }

    public String getGeometry(){
        return geometry;
    }
}
