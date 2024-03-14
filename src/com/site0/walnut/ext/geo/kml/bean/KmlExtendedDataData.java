package com.site0.walnut.ext.geo.kml.bean;

import org.nutz.plugins.xmlbind.annotation.XmlAttr;
import org.nutz.plugins.xmlbind.annotation.XmlEle;

@XmlEle("Data")
public class KmlExtendedDataData {

    @XmlAttr("name")
    public String name;
    
    @XmlEle(simpleNode=true)
    public String value;
}
