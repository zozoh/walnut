package org.nutz.walnut.ext.geo.kml.bean;

import org.nutz.plugins.xmlbind.annotation.XmlEle;

@XmlEle("style")
public class KmlStyleLineStyle {

    @XmlEle(simpleNode=true)
    public String color;
    @XmlEle(simpleNode=true)
    public String width;
}
