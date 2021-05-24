package org.nutz.walnut.ext.net.imap.bean;

import javax.mail.internet.InternetAddress;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.util.Ws;

public class WnEmailAddress {

    private String type;

    private String address;

    private String name;

    public WnEmailAddress() {}

    public WnEmailAddress(InternetAddress iaddr) {
        this.type = iaddr.getType();
        this.address = iaddr.getAddress();
        this.name = iaddr.getPersonal();
        if (Ws.isQuoteBy(this.name, '"', '"')) {
            this.name = name.substring(1, name.length() - 1).trim();
        }
    }

    public NutBean toBean() {
        NutMap bean = new NutMap();
        bean.put("type", type);
        bean.put("address", address);
        bean.put("name", name);
        return bean;
    }

    public String toString() {
        if (Ws.isBlank(name)) {
            return this.address;
        }
        return String.format("%s<%s>", name, address);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String personal) {
        this.name = personal;
    }

}
