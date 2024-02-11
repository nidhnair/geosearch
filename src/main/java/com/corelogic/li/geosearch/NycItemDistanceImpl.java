package com.corelogic.li.geosearch;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.index.strtree.ItemBoundable;
import org.locationtech.jts.index.strtree.ItemDistance;

public class NycItemDistanceImpl implements ItemDistance {
    @Override
    public double distance(ItemBoundable item1, ItemBoundable item2) {
        if (item1 == item2) return Double.MAX_VALUE;
        NYCGeometryWrapper nyc1 = (NYCGeometryWrapper) item1.getItem();
        Geometry g1 =nyc1.getGeometry();
        Geometry g2 = (Geometry) item2.getItem();
        return g1.distance(g2);
    }
}
