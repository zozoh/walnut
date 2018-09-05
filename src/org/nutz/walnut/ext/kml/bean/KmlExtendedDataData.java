package org.nutz.walnut.ext.kml.bean;

import org.nutz.plugins.xmlbind.annotation.XmlAttr;
import org.nutz.plugins.xmlbind.annotation.XmlEle;

@XmlEle("Data")
public class KmlExtendedDataData {

    @XmlAttr("name")
    public String name;
    
    @XmlEle(simpleNode=true)
    public String value;
}
