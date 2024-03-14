package com.site0.walnut.ext.net.sshd.srv;

import java.nio.file.attribute.UserPrincipal;

public class WnJdkUserPrincipal implements UserPrincipal {
    
    protected String name;
    
    public WnJdkUserPrincipal(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

}
