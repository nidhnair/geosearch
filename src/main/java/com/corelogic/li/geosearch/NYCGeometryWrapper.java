package com.corelogic.li.geosearch;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.index.strtree.ItemBoundable;

public class NYCGeometryWrapper extends ItemBoundable {

    private SomeShapesfromNYC someShapesfromNYC;

    private Geometry geometry;

    public NYCGeometryWrapper(Geometry bounds, SomeShapesfromNYC someShapesfromNYC) {
        super(bounds, someShapesfromNYC);
        this.someShapesfromNYC = someShapesfromNYC;
        this.geometry = bounds;
    }

    public SomeShapesfromNYC getSomeShapesfromNYC() {
        return someShapesfromNYC;
    }

    public void setSomeShapesfromNYC(SomeShapesfromNYC someShapesfromNYC) {
        this.someShapesfromNYC = someShapesfromNYC;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

}
