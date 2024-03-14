package com.site0.walnut.ext.geo.kml.bean;

import java.util.List;

import org.nutz.plugins.xmlbind.annotation.XmlEle;

@XmlEle("gx:Track")
public class KmlGxTrack {

    @XmlEle(value="gx:coord", simpleNode=true)
    public List<String> coords;
    
    @XmlEle(value="when", simpleNode=true)
    public List<String> whens;
}
