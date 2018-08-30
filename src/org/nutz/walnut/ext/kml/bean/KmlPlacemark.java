package org.nutz.walnut.ext.kml.bean;

import org.nutz.plugins.xmlbind.annotation.XmlEle;

@XmlEle("Placemark")
public class KmlPlacemark {

    @XmlEle(simpleNode=true, value="Name")
    public String name;
    @XmlEle(simpleNode=true)
    public String styleUrl;
    @XmlEle("LineString")
    public KmlPlacemarkLineString lineString;
    @XmlEle("Point")
    public KmlPlacemarkPoint point;
}
