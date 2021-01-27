package org.nutz.walnut.ext.www.impl;

import java.util.HashMap;
import java.util.Map;

public class VirtualPageFactory {

    private Map<String, VirtualPage> pages;

    public VirtualPageFactory() {
        pages = new HashMap<>();
    }

    public VirtualPage get(String str) {
        VirtualPage page = pages.get(str);
        if (null == page) {
            synchronized (this) {
                page = pages.get(str);
                if (null == page) {
                    page = new VirtualPage(str);
                    pages.put(str, page);
                }
            }
        }
        return page;
    }

}
