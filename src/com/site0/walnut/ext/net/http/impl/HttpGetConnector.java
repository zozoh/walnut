package com.site0.walnut.ext.net.http.impl;

import com.site0.walnut.ext.net.http.HttpContext;

public class HttpGetConnector extends AbstractHttpConnector {

    public HttpGetConnector(HttpContext hc) {
        super(hc);
    }

    @Override
    public void prepare() {
        if (hc.hasParams()) {
            hc.addQuery(hc.getParams());
        }
    }

    @Override
    public void sendBody() {}

}
