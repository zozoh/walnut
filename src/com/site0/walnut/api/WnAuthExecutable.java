package com.site0.walnut.api;

import org.nutz.lang.util.Callback;

import com.site0.walnut.login.usr.WnUser;

public interface WnAuthExecutable extends WnExecutable {

    void switchUser(WnUser newUsr, Callback<WnAuthExecutable> callback);

}
