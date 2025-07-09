package com.site0.walnut.login.usr;

public abstract class WnUsers {

    public static boolean isValidAccountName(String nm) {
        return null != nm && nm.matches("^[0-9a-zA-Z_]{2,}$");
    }
}
