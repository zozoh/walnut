package org.nutz.walnut.ext.geo.kml.bean;

import org.nutz.plugins.xmlbind.annotation.XmlEle;

@XmlEle("IconStyle")
public class KmlStyleIconStyle {

    @XmlEle("Icon")
    public KmlStyleIconStyleIcon icon;
}