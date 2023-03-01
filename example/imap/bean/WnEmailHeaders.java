package org.nutz.walnut.ext.net.imap.bean;

import java.util.Enumeration;

import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.Part;

import org.nutz.lang.util.NutMap;

public class WnEmailHeaders extends NutMap {

    public WnEmailHeaders() {}

    public WnEmailHeaders(Part part) throws MessagingException {
        this(part.getAllHeaders());
    }

    public WnEmailHeaders(Enumeration<Header> headers) {
        while (headers.hasMoreElements()) {
            Header he = headers.nextElement();
            this.put(he.getName(), he.getValue());
        }
    }

}
