package org.nutz.walnut.api.auth;

public abstract class WnAuths {

    
    public static boolean isValidUserName(String nm) {
        return null != nm && nm.matches("^[0-9a-zA-Z_]{2,}$");
    }
    
}
