package com.site0.walnut.login.session;

import java.util.Map;

import org.nutz.lang.util.NutMap;

import com.site0.walnut.login.usr.WnUserStore;

public abstract class AbstractWnSessionStore implements WnSessionStore {

    protected NutMap defaultEnv;

    @Override
    public void patchDefaultEnv(WnSession se) {
        if (null == this.defaultEnv) {
            return;
        }
        // 整体设置默认值
        if (null == se.getEnv()) {
            se.setEnv(this.defaultEnv.duplicate());
        }
        // 逐个设置默认值
        else {
            for (Map.Entry<String, Object> en : this.defaultEnv.entrySet()) {
                String key = en.getKey();
                Object val = en.getValue();
                se.getEnv().putDefault(key, val);
            }
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

            WnSession pse = this.getSession(parentTicket, users);
            pse.setChildTicket(se.getTicket());
            this.saveSessionChildTicket(pse);
            return pse;
        }

        return null;
    }

}
