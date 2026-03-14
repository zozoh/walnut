package com.site0.walnut.web;

import org.nutz.mvc.Mvcs;
import org.nutz.web.WebLauncher;

public class WnLauncher extends WebLauncher {

    public static void main(String[] args) {
        Mvcs.DISABLE_X_POWERED_BY = true;
        WebLauncher.start(args);
    }

}