package com.site0.walnut.api;

import org.nutz.lang.util.Callback;
import com.site0.walnut.api.auth.WnAccount;

public interface WnAuthExecutable extends WnExecutable {

    void switchUser(WnAccount newUsr, Callback<WnAuthExecutable> callback);

}
