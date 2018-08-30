package org.nutz.walnut.ext.kml.bean;

import org.nutz.plugins.xmlbind.annotation.XmlEle;

@XmlEle("Point")
public class KmlPlacemarkPoint {

    @XmlEle(simpleNode=true)
    public String coordinates;
}
