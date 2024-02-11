package com.corelogic.li.geosearch;

import org.locationtech.jts.geom.Geometry;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

public class SomeShapesfromNYC  {

    TimeZone tz = TimeZone.getTimeZone("CST");
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'"); // Quoted "Z" to indicate UTC, no timezone offset

    private String id;
    private String location;

    private String datetime;

    private Map<String, String> attributeMap;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDatetime(){
//        df.setTimeZone(tz);
//        return df.format(new Date());

        return String.valueOf(System.currentTimeMillis());
    }


    public Map<String, String> getAttributeMap() {
        return attributeMap;
    }

    public void setAttributeMap(Map<String, String> attributeMap) {
        this.attributeMap = attributeMap;
    }
}
