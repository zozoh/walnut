package org.nutz.walnut.ext.sshd.srv;

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
