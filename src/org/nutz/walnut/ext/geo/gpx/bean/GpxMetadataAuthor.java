package org.nutz.walnut.ext.geo.gpx.bean;

import org.nutz.plugins.xmlbind.annotation.XmlEle;

@XmlEle("author")
public class GpxMetadataAuthor {

    @XmlEle(simpleNode=true)
    protected String name;
    @XmlEle(simpleNode=true)
    protected String email;
    @XmlEle
    protected GpxMetadataAuthorLink link;
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public GpxMetadataAuthorLink getLink() {
        return link;
    }
    public void setLink(GpxMetadataAuthorLink link) {
        this.link = link;
    }
    
}
