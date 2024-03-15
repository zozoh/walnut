package com.site0.walnut.impl.box.cmd;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.util.NutMap;
import org.nutz.trans.Proton;
import com.site0.walnut.api.auth.WnAccount;
import com.site0.walnut.api.auth.WnAuthSession;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.www.impl.WnWebService;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.impl.io.WnEvalLink;
import com.site0.walnut.impl.srv.WnDomainService;
import com.site0.walnut.impl.srv.WwwSiteInfo;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.WnContext;
import com.site0.walnut.util.ZParams;
import com.site0.walnut.util.Wn.Session;

public class cmd_login extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        Wn.WC().security(new WnEvalLink(sys.io), () -> {
            __exec_without_security(sys, args);
        });

    }

    private WnAccount __load_account(WnSystem sys, ZParams params, WwwSiteInfo si) {
        String uname = params.val_check(0);
        if (null != si) {
            return si.webs.getAuthApi().checkAccount(uname);
        }
        return sys.auth.checkAccount(uname);
    }

    private void __exec_without_security(WnSystem sys, String[] args) {
        // 解析参数
        ZParams params = ZParams.parse(args, "cnqH");

        WnAccount me = sys.getMe();
        WnAuthSession newSe;
        WnContext wc = Wn.WC();

        // 子站点登录
        WwwSiteInfo si = null;
        if (params.has("site")) {
            WnDomainService domains = new WnDomainService(sys.io);
            String site = params.getString("site");
            Pattern _P = Pattern.compile("^(host|id):(.+)$");
            Matcher m = _P.matcher(site);
            // 直接指定了 siteId 或者 host
            if (m.find()) {
                String siteId = null, host = null;
                if ("id".equals(m.group(1))) {
                    siteId = m.group(2);
                } else {
                    host = m.group(2);
                }
                String sid = siteId;
                String hnm = host;
                si = wc.suCoreNoSecurity(sys.io, me, new Proton<WwwSiteInfo>() {
                    protected WwwSiteInfo exec() {
                        return domains.getWwwSiteInfo(sid, hnm);
                    }
                });
            }
            // 直接采用站点路径
            else {
                if ("true".equals(site)) {
                    site = null;
                }
                si = domains.getWwwSiteInfoByHome(sys.getHome(), site);
            }
        }

        // 得到用户
        WnAccount ta = __load_account(sys, params, si);

        // ............................................
        // 开始检查权限了

        // 自己不能登录到自己
        if (me.isSame(ta)) {
            throw Er.create("e.cmd.login.self", me.getName());
        }
        // 域管理员可以登录到域的子账号
        if (null != si) {
            if (!sys.auth.isAdminOfGroup(me, sys.getMyGroup())) {
                throw Er.create("e.cmd.login.me.forbid", "Need Admin of Domain");
            }
            String byType = WnAuthSession.V_BT_AUTH_BY_DOMAIN;
            String byValue = si.siteId + ":passwd";

            // 确保用户是可以访问域主目录的
            Session.checkHomeAccessable(sys.io, sys.auth, si.oHome, ta);

            // 获取会话时长设置
            WnWebService webs = si.webs;
            WnObj oWWW = si.oWWW;
            int se_du = webs.getSite().getSeDftDu();
            newSe = wc.suCoreNoSecurity(sys.io, me, new Proton<WnAuthSession>() {
                protected WnAuthSession exec() {
                    // 创建会话
                    WnAuthSession se = sys.auth.createSession(sys.session, ta, se_du);
                    // 更新会话元数据
                    Session.updateAuthSession(sys.auth, null, se, webs, oWWW, byType, byValue);
                    return se;
                }
            });
        }
        // root 用户可以登录到任何用户
        else if (me.isRoot()) {
            // 嗯，可以登录
            newSe = sys.auth.createSession(sys.session, ta, 0);
        }
        // root 组管理员能登录到除了 root 组管理员之外任何账户
        else if (sys.auth.isAdminOfGroup(me, "root") && !sys.auth.isAdminOfGroup(ta, "root")) {
            // 嗯，可以登录
            newSe = sys.auth.createSession(sys.session, ta, 0);
        }
        // 否则执行操作的用户必须为 root|op 组成员
        // 目标用户必须不能为 root|op 组成员
        else {
            if (!sys.auth.isMemberOfGroup(me, "root", "op")) {
                throw Er.create("e.cmd.login.me.forbid");
            }
            if (sys.auth.isMemberOfGroup(ta, "root", "op")) {
                throw Er.create("e.cmd.login.ta.forbid");
            }
            newSe = sys.auth.createSession(sys.session, ta, 0);
        }

        // 输出这个新会话
        JsonFormat jfmt = Cmds.gen_json_format(params);
        NutMap bean = newSe.toMapForClient();
        String json = Json.toJson(bean, jfmt);
        sys.out.println(json);

        // ............................................
        // 在沙盒的上下文标记一把
        sys.attrs().put(Wn.MACRO.CHANGE_SESSION, Wlang.map("seid", newSe.getTicket()));
    }

}
