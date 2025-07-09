package com.site0.walnut.login.session;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.login.WnSession;
import com.site0.walnut.login.WnSessionStore;
import com.site0.walnut.login.WnUserStore;

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

    public NutMap getDefaultEnv() {
        return defaultEnv;
    }

    public void setDefaultEnv(NutMap defaultEnv) {
        this.defaultEnv = defaultEnv;
    }

    protected abstract void _remove_session(WnSession se);

    @Override
    public WnSession reomveSession(WnSession se, WnUserStore users) {
        // 移除当前会话
        _remove_session(se);

        // 如果有父会话，则返回父会话
        if (se.hasParentTicket()) {
            String parentTicket = se.getParentTicket();
            return this.getSession(parentTicket, users);
        }

        return null;
    }

}
