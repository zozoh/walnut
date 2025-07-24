package com.site0.walnut.login.session;

import java.util.List;
import java.util.Map;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.login.site.WnLoginSite;
import com.site0.walnut.login.usr.WnLazyUser;
import com.site0.walnut.login.usr.WnUserStore;

public abstract class AbstractWnSessionStore implements WnSessionStore {

    protected WnIo io;
    protected NutBean sessionVars;
    protected NutMap defaultEnv;

    protected AbstractWnSessionStore(WnIo io, NutBean sessionVars, NutMap defaultEnv) {
        this.io = io;
        this.sessionVars = sessionVars;
        this.defaultEnv = defaultEnv;
    }

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

    protected abstract WnSession _get_one(String ticket);

    protected abstract WnSession _find_one_by_uid_type(String unm, String type);

    protected abstract WnSession _find_one_by_unm_type(String uid, String type);

    protected abstract List<WnSession> _query(NutMap filter, NutMap sorter, int skip, int limit);

    @Override
    public List<WnSession> querySession(int limit, WnUserStore users) {
        List<WnSession> list = _query(null, null, 0, limit);

        for (WnSession se : list) {
            __setup_se(users, se);
        }

        return list;
    }

    @Override
    public WnSession getSession(String ticket, WnUserStore users) {
        WnSession se = _get_one(ticket);

        __setup_se(users, se);

        // 返回结果
        return se;
    }

    @Override
    public WnSession getSessionByUserIdAndType(String uid, String type, WnUserStore users) {
        WnSession se = _find_one_by_unm_type(uid, type);

        __setup_se(users, se);

        // 返回结果
        return se;
    }

    @Override
    public WnSession getSessionByUserNameAndType(String unm, String type, WnUserStore users) {
        WnSession se = _find_one_by_unm_type(unm, type);

        __setup_se(users, se);

        // 返回结果
        return se;
    }

    // 补全会话的用户等其他属性
    private void __setup_se(WnUserStore users, WnSession se) {
        if (null == se) {
            return;
        }
        // 如果会话指定了站点，采用站点指明的用户存储策略
        if (se.hasSite()) {
            String sitePath = se.getSite();
            WnLoginSite site = WnLoginSite.create(io, sitePath, null);
            users = site.auth().getUserStore();
        }

        // 组合完用户加载条件
        WnLazyUser u = (WnLazyUser) se.getUser();
        u.setLoader(users);
        se.loadEnvFromUser(null);
    }

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
