package org.nutz.walnut.ext.kml.bean;

import java.util.List;

import org.nutz.plugins.xmlbind.annotation.XmlEle;

@XmlEle("ExtendedData")
public class KmlExtendedData {

    @XmlEle("Data")
    public List<KmlExtendedDataData> data;
}
