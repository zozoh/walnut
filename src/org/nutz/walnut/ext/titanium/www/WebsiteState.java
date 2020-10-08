package org.nutz.walnut.ext.titanium.www;

import java.util.HashMap;

public class WebsiteState {

    private HashMap<String, WebsiteApi> apis;

    public HashMap<String, WebsiteApi> getApis() {
        return apis;
    }

    public void setApis(HashMap<String, WebsiteApi> apis) {
        this.apis = apis;
    }

}
