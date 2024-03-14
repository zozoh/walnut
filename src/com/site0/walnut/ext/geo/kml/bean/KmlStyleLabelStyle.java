package com.site0.walnut.ext.geo.kml.bean;

import org.nutz.plugins.xmlbind.annotation.XmlEle;

@XmlEle("IconStyle")
public class KmlStyleLabelStyle {

    @XmlEle(simpleNode=true)
    public String color;
}
