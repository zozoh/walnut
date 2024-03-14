package com.site0.walnut.ext.geo.kml.bean;

import java.util.List;

import org.nutz.plugins.xmlbind.annotation.XmlAttr;
import org.nutz.plugins.xmlbind.annotation.XmlEle;

@XmlEle("Folder")
public class KmlFolder2 {

    @XmlAttr
    public String id;
    
    @XmlEle(simpleNode=true)
    public String name;
    
    @XmlEle("Placemark")
    public List<KmlPlacemark> placemarks;
}
