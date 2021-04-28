package org.nutz.walnut.ext.geo.kml.bean;

import org.nutz.plugins.xmlbind.annotation.XmlEle;

public class KmlPlacemarkLineString {

    @XmlEle(simpleNode=true)
    public String tessellate;
    @XmlEle(simpleNode=true)
    public String altitudeMode;
    @XmlEle(simpleNode=true)
    public String coordinates;
}
