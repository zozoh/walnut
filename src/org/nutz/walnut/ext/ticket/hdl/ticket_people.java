package org.nutz.walnut.ext.ticket.hdl;

import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

/**
 * 管理工单相关用户，客服
 * 
 * @author pw
 *
 */
public class ticket_people implements JvmHdl {

    private Log log = Logs.get();

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        ZParams params = ZParams.parse(hc.args, null);

        WnObj ticketHome = sys.io.fetch(null, Wn.normalizeFullPath("~/.ticket", sys));
        if (ticketHome == null) {
            sys.err.print("ticket not init, please exec 'ticket init'");
            return;
        }

        String tp = params.get("tp", "user"); // user|cservice
        WnObj peoDir = sys.io.check(ticketHome, tp);

        // 添加
        if (params.has("add")) {
            WnUsr wnUsr = getWnUser(sys, params.getString("add"));

            // 检查用户是否存在
            String pNm = "wn_" + wnUsr.id();
            if (sys.io.exists(peoDir, pNm)) {
                sys.err.printf("ticket has %s reg by [%s]", tp, params.getString("add"));
                return;
            }
            // 新建并初始化
            WnObj cjson = sys.io.fetch(null,
                                       Wn.normalizeFullPath("/etc/init/ticket/tmpl_" + tp + ".json",
                                                            sys));
            NutMap uConf = sys.io.readJson(cjson, NutMap.class);
            uConf.setv("usrId", wnUsr.id());
            uConf.setv("usrNm", wnUsr.name());
            uConf.setv("usrDmn", wnUsr.home().replaceAll("/home/", ""));
            WnObj tPeople = sys.io.create(peoDir, pNm, WnRace.FILE);
            sys.io.appendMeta(tPeople, uConf);
        }

        // 更新
        if (params.has("update")) {
            WnUsr wnUsr = getWnUser(sys, params.getString("update"));
            // 检查用户是否存在
            String pNm = "wn_" + wnUsr.id();
            if (!sys.io.exists(peoDir, pNm)) {
                sys.err.printf("ticket not has %s reg by [%s]", tp, params.getString("update"));
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
            List<WnObj> pList = sys.io.query(wnQuery);
            sys.out.println(Json.toJson(pList));
        }

    }

    private WnUsr getWnUser(WnSystem sys, String ustr) {
        // 只允许通过id查询，防止通过用户名，电话，email等方式
        return sys.usrService.check("id:" + ustr);
    }

}
