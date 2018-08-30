package org.nutz.walnut.ext.gpx.bean;

import org.nutz.plugins.xmlbind.annotation.XmlEle;

@XmlEle("extensions")
public class GpxRteExtension {

    @XmlEle(simpleNode = true)
    public String width;

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }
    
}
