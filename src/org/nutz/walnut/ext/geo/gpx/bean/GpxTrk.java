package org.nutz.walnut.ext.geo.gpx.bean;

import org.nutz.plugins.xmlbind.annotation.XmlEle;

@XmlEle("trk")
public class GpxTrk {

    @XmlEle(simpleNode=true)
    public String name;
    @XmlEle
    public GpxTrkseg trkseg;
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public GpxTrkseg getTrkseg() {
        return trkseg;
    }
    public void setTrkseg(GpxTrkseg trkseg) {
        this.trkseg = trkseg;
    }

    
}
