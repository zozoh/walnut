package org.nutz.walnut.api;

import org.nutz.lang.util.Callback;
import org.nutz.walnut.api.WnExecutable;
import org.nutz.walnut.api.auth.WnAccount;

public interface WnAuthExecutable extends WnExecutable {

    void switchUser(WnAccount newUsr, Callback<WnAuthExecutable> callback);

}
