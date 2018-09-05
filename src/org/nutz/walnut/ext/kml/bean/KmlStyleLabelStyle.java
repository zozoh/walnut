package org.nutz.walnut.ext.kml.bean;

import org.nutz.plugins.xmlbind.annotation.XmlEle;

@XmlEle("IconStyle")
public class KmlStyleLabelStyle {

    @XmlEle(simpleNode=true)
    public String color;
}
