package org.nutz.walnut.ext.geo.kml.bean;

import org.nutz.plugins.xmlbind.annotation.XmlAttr;
import org.nutz.plugins.xmlbind.annotation.XmlEle;

@XmlEle("Placemark")
public class KmlPlacemark {

    @XmlAttr("id")
    public String id;
    @XmlEle(simpleNode=true, value="name")
    public String name;
    @XmlEle(simpleNode=true)
    public String description;
    @XmlEle(simpleNode=true)
    public String styleUrl;
    @XmlEle("LineString")
    public KmlPlacemarkLineString lineString;
    @XmlEle("Point")
    public KmlPlacemarkPoint point;
    @XmlEle("ExtendedData")
    public KmlExtendedData extendedData;
    @XmlEle("gx:Track")
    public KmlGxTrack track;
    @XmlEle("TimeStamp")
    public KmlTimeStamp TimeStamp;
    @XmlEle("MultiGeometry")
    public KmlMultiGeometry MultiGeometry;
}