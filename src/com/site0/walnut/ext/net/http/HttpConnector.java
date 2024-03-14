package com.site0.walnut.ext.net.http;

import java.io.IOException;

import com.site0.walnut.ext.net.http.bean.WnHttpResponse;

public interface HttpConnector {

    void prepare();

    void connect() throws IOException;

    void sendHeaders();

    abstract void sendBody() throws IOException;

    WnHttpResponse getResponse() throws IOException;

}
