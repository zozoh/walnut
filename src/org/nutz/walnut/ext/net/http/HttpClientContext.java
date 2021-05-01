package org.nutz.walnut.ext.net.http;

import org.nutz.walnut.impl.box.JvmFilterContext;

public class HttpClientContext extends JvmFilterContext {

    public HttpContext context;
    
    
    public HttpClientContext() {
        this.context = new HttpContext();
    }
}
