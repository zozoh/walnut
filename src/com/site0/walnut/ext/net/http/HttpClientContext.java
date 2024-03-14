package com.site0.walnut.ext.net.http;

import com.site0.walnut.impl.box.JvmFilterContext;

public class HttpClientContext extends JvmFilterContext {

    public HttpContext context;
    
    
    public HttpClientContext() {
        this.context = new HttpContext();
    }
}
