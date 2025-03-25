package com.site0.walnut.ext.net.webx;

import org.nutz.web.WebException;

import com.site0.walnut.impl.box.JvmFilterContext;
import com.site0.walnut.login.WnLoginApi;

public class WebxContext extends JvmFilterContext {

    public WnLoginApi api;

    public Object result;

    public WebException error;

    public boolean quiet;

}
