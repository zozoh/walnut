package org.nutz.walnut.ext.kml.bean;

import org.nutz.plugins.xmlbind.annotation.XmlAttr;
import org.nutz.plugins.xmlbind.annotation.XmlEle;

@XmlEle("Style")
public class KmlStyle {

    @XmlAttr
    public String id;
    @XmlEle("LineStyle")
    public KmlStyleLineStyle lineStyle;
}
