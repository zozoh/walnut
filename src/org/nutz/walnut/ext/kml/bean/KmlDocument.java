package org.nutz.walnut.ext.kml.bean;

import java.util.List;

import org.nutz.plugins.xmlbind.annotation.XmlEle;

@XmlEle("Document")
public class KmlDocument {

    @XmlEle(simpleNode=true)
    public String name;
    @XmlEle(simpleNode=true)
    public String open;
    @XmlEle("Style")
    public List<KmlStyle> styles;
    @XmlEle("Placemark")
    public List<KmlPlacemark> placemarks;
    
    @XmlEle("ExtendedData")
    public KmlExtendedData extendedData;
    
    @XmlEle("Folder")
    public List<KmlFolder> folders;
    
    
}
