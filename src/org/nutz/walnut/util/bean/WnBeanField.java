package org.nutz.walnut.util.bean;

import org.nutz.walnut.util.Ws;

public class WnBeanField extends WnValue {

    private String name;

    public String getName() {
        return name;
    }

    public String getName(String dftName) {
        return Ws.sBlank(name, dftName);
    }

    public void setName(String name) {
        this.name = name;
    }

}
