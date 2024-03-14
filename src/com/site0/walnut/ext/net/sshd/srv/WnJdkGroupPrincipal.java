package com.site0.walnut.ext.net.sshd.srv;

import java.nio.file.attribute.GroupPrincipal;

public class WnJdkGroupPrincipal implements GroupPrincipal {
    
    protected String name;
    
    public WnJdkGroupPrincipal(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

}
