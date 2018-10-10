package org.nutz.walnut.ext.kml.bean;

import org.nutz.plugins.xmlbind.annotation.XmlAttr;
import org.nutz.plugins.xmlbind.annotation.XmlEle;

@XmlEle("kml")
public class KmlFile {

    @XmlAttr
    public String xmlns = "http://earth.google.com/kml/2.1";
    
    @XmlEle("Document")
    public KmlDocument document;
}
