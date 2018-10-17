package org.nutz.walnut.ext.kml.bean;

import org.nutz.plugins.xmlbind.annotation.XmlEle;


@XmlEle("TimeStamp")
public class KmlTimeStamp {

    @XmlEle(value="when", simpleNode=true)
    public String when;
}
