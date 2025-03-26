package com.site0.walnut.login.session;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.login.WnSession;
import com.site0.walnut.login.WnSessionStore;

public abstract class AbstractWnSessionStore implements WnSessionStore {

    protected NutMap defaultEnv;

    public void patchDefaultEnv(WnSession se) {
        NutBean env = null == this.defaultEnv ? new NutMap() : this.defaultEnv.duplicate();
        if (null == se.getEnv()) {
            se.setEnv(env);
        }
        // 融合
        else {
            se.getEnv().putAll(env);
        }
    }

}
