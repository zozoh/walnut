package org.nutz.walnut.ext.geo.kml.bean;

import org.nutz.plugins.xmlbind.annotation.XmlEle;


@XmlEle("TimeStamp")
public class KmlTimeStamp {

    @XmlEle(value="when", simpleNode=true)
    public String when;
}
