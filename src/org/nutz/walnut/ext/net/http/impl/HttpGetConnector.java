package org.nutz.walnut.ext.net.http.impl;

import org.nutz.walnut.ext.net.http.HttpContext;

public class HttpGetConnector extends AbstractHttpConnector {

    public HttpGetConnector(HttpContext hc) {
        super(hc);
    }

    @Override
    public void sendBody() {}

}
