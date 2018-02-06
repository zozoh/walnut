package org.nutz.walnut.ext.ticket.hdl;

import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.trans.Proton;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

import com.beust.jcommander.Strings;

/**
 * 管理工单相关用户，客服
 * 
 * 
 * @author pw
 *
 */
public class ticket_people implements JvmHdl {

    private Log log = Logs.get();

    private static String ADD_GRP = "grp %s -a %s -role 10";

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        ZParams params = ZParams.parse(hc.args, null);

        String ts = params.get("ts", "");
        String tp = params.get("tp", "user"); // user|cservice
        boolean isCS = "cservice".equals(tp);
        String thString = Strings.isStringEmpty(ts) ? "~/.ticket" : "~/.ticket_" + ts;
        WnObj ticketHome = sys.io.fetch(null, Wn.normalizeFullPath(thString, sys));
        if (ticketHome == null) {
            sys.err.printf("e.ticket: data dir [%s] not found, please exec 'ticket init'",
                           thString);
            return;
        }

        WnObj peoDir = sys.io.check(ticketHome, tp);

        // 添加
        if (params.has("add")) {
            WnUsr wnUsr = getWnUser(sys, params.getString("add"));

            // 检查用户是否存在
            WnObj tPeople = null;
            String pNm = "wn_" + wnUsr.id();
            if (sys.io.exists(peoDir, pNm)) {
                // sys.err.printf("e.ticket: has %s reg by [%s]", tp,
                // params.getString("add"));
                tPeople = sys.io.fetch(peoDir, pNm);
            } else {
                // 新建并初始化
                WnObj cjson = sys.io.fetch(null,
                                           Wn.normalizeFullPath("/etc/init/ticket/tmpl_"
                                                                + tp
                                                                + ".json",
                                                                sys));
                NutMap uConf = sys.io.readJson(cjson, NutMap.class);
                uConf.setv("usrId", wnUsr.id());
                uConf.setv("usrNm", wnUsr.name());
                uConf.setv("usrDmn", wnUsr.home().replaceAll("/home/", ""));
                if (isCS) {
                    uConf.setv("usrAlias", uConf.getString("usrAlias", "") + wnUsr.name());
                }
                tPeople = sys.io.create(peoDir, pNm, WnRace.FILE);
                sys.io.appendMeta(tPeople, uConf);

                // 加入到我的组，可以访问.ticket内容
                if (!wnUsr.home().equals(sys.me.home())) {
                    // 加入组，然后可以访问自己的对象
                    sys.execf(ADD_GRP, sys.me.name(), wnUsr.name());
                    NutMap pvg = NutMap.NEW();
                    pvg.setv(wnUsr.name(), 5);
                    sys.io.appendMeta(tPeople, NutMap.NEW().setv("pvg", pvg));
                }
            }

            // 返回指定内容
            WnObj cfObj = sys.io.fetch(ticketHome, "ticket.json");
            NutMap reJson = sys.io.readJson(cfObj, NutMap.class);
            reJson.setv("notiObj", tPeople.id());
            sys.out.print(Json.toJson(reJson));
        }

        // 更新
        if (params.has("update")) {
            WnUsr wnUsr = getWnUser(sys, params.getString("update"));
            // 检查用户是否存在
            String pNm = "wn_" + wnUsr.id();
            if (!sys.io.exists(peoDir, pNm)) {
                sys.err.printf("e.ticket: not has %s reg by [%s]", tp, params.getString("update"));
                return;
            }
            WnObj tPeople = sys.io.fetch(peoDir, pNm);
            NutMap umeta = Lang.map(params.get("c"));
            sys.io.appendMeta(tPeople, umeta);
        }

        // 查询
        if (params.has("query")) {
            NutMap qvars = Lang.map(params.getString("query"));
            WnQuery wnQuery = new WnQuery();
            wnQuery.add(qvars);
            wnQuery.setv("pid", peoDir.id());
            wnQuery.sortBy("nm", 1);
            wnQuery.sortBy("ct", -1);
            List<WnObj> pList = sys.io.query(wnQuery);
            sys.out.println(Json.toJson(pList));
        }

    }

    private WnUsr getWnUser(WnSystem sys, String ustr) {
        // 只允许通过id查询，防止通过用户名，电话，email等方式
        Proton<WnUsr> proton = new Proton<WnUsr>() {
            @Override
            protected WnUsr exec() {
                return sys.usrService.check("id:" + ustr);
            }
        };
        sys.nosecurity(proton);
        return proton.get();
    }

}
