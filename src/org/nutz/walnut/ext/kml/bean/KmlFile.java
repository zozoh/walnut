package org.nutz.walnut.ext.kml.bean;

import org.nutz.plugins.xmlbind.annotation.XmlEle;

@XmlEle("kml")
public class KmlFile {

    @XmlEle("Document")
    public KmlDocument document;
}
