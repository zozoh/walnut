package org.nutz.walnut.ext.net.http;

import java.io.IOException;

import org.nutz.walnut.ext.net.http.bean.WnHttpResponse;

public interface HttpConnector {

    void connect() throws IOException;

    void sendHeaders();

    abstract void sendBody();

    WnHttpResponse getResponse() throws IOException;

}
