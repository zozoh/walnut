package org.nutz.walnut.ext.geo.kml.bean;

import org.nutz.plugins.xmlbind.annotation.XmlAttr;
import org.nutz.plugins.xmlbind.annotation.XmlEle;

@XmlEle("kml")
public class KmlFile {

    // <kml xmlns="http://www.opengis.net/kml/2.2" xmlns:gx="http://www.google.com/kml/ext/2.2">
    @XmlAttr
    public String xmlns = "http://www.opengis.net/kml/2.2";
    
    @XmlAttr("xmlns:gx")
    public String xmlns_gx = "http://www.google.com/kml/ext/2.2";
    
    @XmlEle("Document")
    public KmlDocument document;
}
