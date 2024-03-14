package com.site0.walnut.ext.geo.kml.bean;

import org.nutz.plugins.xmlbind.annotation.XmlEle;

@XmlEle("MultiGeometry")
public class KmlMultiGeometry {

    @XmlEle("LineString")
    public KmlPlacemarkLineString LineString; 
}
