package com.site0.walnut.web2.controller.httpapi;

import org.nutz.lang.util.NutMap;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.ext.data.pvg.BizPvgService;
import com.site0.walnut.ext.data.pvg.WnAuthOptions;
import com.site0.walnut.login.WnLoginApiMaker;
import com.site0.walnut.login.role.WnRoleList;
import com.site0.walnut.login.role.WnRoleType;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;

/**
 * RM-NUTZ-09：照抄旧 {@code 旧 HTTP API 模块} 的站点认证/权限方法组：
 * {@code __do_www_auth}（879-919）、{@code __do_www_check_pvg}（286-334）。
 */
public class ApiWwwAuth {

    private final WnIo io;

    public ApiWwwAuth(WnIo io) {
        this.io = io;
    }

    public void doWwwAuth(WnHttpApiContext apc) {
        String phWWW = apc.oApi.getString("http-www-home");
        phWWW = (String) Wn.explainObj(apc.reqMeta, phWWW);
        boolean hasWWWHome = !Ws.isBlank(phWWW);
        apc.isNeedWWWAuth = apc.oApi.getBoolean("http-www-auth", hasWWWHome);
        apc.wwwSe = null;
        if (hasWWWHome) {
            String ticketBy = apc.oApi.getString("http-www-ticket",
                                                 "http-qs-ticket");
            String ticket = apc.reqMeta.getString(ticketBy);
            if (!Ws.isBlank(ticket)) {
                apc.oWWW = Wn.checkObj(io, apc.se, phWWW);
                // 准备站点: 文件模式
                if (apc.oWWW.isFILE()) {
                    apc.authOptions = io.readJson(apc.oWWW,
                                                  WnAuthOptions.class);
                }
                // 准备站点: 目录模式
                else {
                    apc.authOptions = Wlang.map2Object(apc.oWWW,
                                                       WnAuthOptions.class);
                }
                // 创建权鉴接口
                apc.loginApi = WnLoginApiMaker.forDomain()
                    .make(io, apc.se.getEnv(), apc.authOptions);
                apc.wwwSe = apc.loginApi.checkSession(ticket);
                apc.wwwMe = apc.wwwSe.getUser();

                // 会话信息
                apc.reqMeta.put("http-www-se-ticket", apc.wwwSe.getTicket());
                // 用户信息
                apc.reqMeta.put("http-www-me-id", apc.wwwMe.getId());
                apc.reqMeta.put("http-www-me-nm", apc.wwwMe.getName());
                apc.reqMeta.put("http-www-me", apc.wwwMe.getMeta());
                apc.reqMeta.put("http-www-me-phone", apc.wwwMe.getPhone());
                apc.reqMeta.put("http-www-me-email", apc.wwwMe.getEmail());
                apc.reqMeta.put("http-www-me-mainGroup",
                                apc.wwwMe.getMainGroup());
            }
        }
        // 检查一下权限
        if (null == apc.wwwSe && apc.isNeedWWWAuth) {
            throw Er.create("e.api.forbid");
        }
    }

    public void doWwwCheckPvg(WnHttpApiContext apc) {
        // !! 进行到这里，一定已经经过了 doWwwAuth 的检查
        // !! 也就是说， wwwSe 与 wwwMe 还有 loginApi 都一定不是空值
        String[] assActions = apc.oApi.getArray("pvg-assert", String.class);

        // 没有声明检查项，不捡
        if (null == assActions) {
            return;
        }

        // 木有检查项，那就是全都不通过
        if (assActions.length == 0) {
            throw Er.create("e.api.forbid");
        }

        // 防空
        if (!apc.isNeedWWWAuth || null != apc.authOptions) {
            return;
        }

        String phPvg = apc.oApi.getString("pvg-setup");
        // 如果木有设置，那么看看站点有没有设置
        if (Ws.isBlank(phPvg)) {
            phPvg = apc.authOptions.pvgSetup;
        }

        // 没设置不检
        if (Ws.isBlank(phPvg)) {
            return;
        }

        // 读一下设置
        apc.oPvgSetup = Wn.checkObj(io, apc.se, phPvg);
        NutMap setup = io.readJson(apc.oPvgSetup, NutMap.class);
        apc.bizPvgs = new BizPvgService(setup);

        // 得到当前账户的角色
        WnRoleList roles = apc.getWWWRoles();
        String[] roleNames = roles.getRoleNames(WnRoleType.MEMBER);

        // 开始检查咯
        for (String assA : assActions) {
            String[] actions = Ws.splitIgnoreBlank(assA, "[|]+");
            if (!apc.bizPvgs.canOneOf(roleNames, actions)) {
                throw Er.create("e.api.forbid");
            }
        }

    }

}
