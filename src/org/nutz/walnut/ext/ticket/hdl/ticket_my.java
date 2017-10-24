package org.nutz.walnut.ext.ticket.hdl;

import org.nutz.http.Http;
import org.nutz.http.Response;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;
import org.nutz.web.ajax.Ajax;
import org.nutz.web.ajax.AjaxReturn;

/**
 * 管理我的工单，普通用户只能通过该接口与工单系统交互
 * 
 * 一个用户可以同时注册不同的工单系统
 * 
 * @author pw
 * 
 */

public class ticket_my implements JvmHdl {

    private Log log = Logs.get();

    // api
    private static String API_TMPL = "http://%s/api/%s";
    //
    private static String API_REG = "/ticket/reg";

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        ZParams params = ZParams.parse(hc.args, null);
        // 需要指定工单系统的名称
        String service = params.get("s", "127.0.0.1");
        String ts = params.get("ts", sys.getHome().name());
        String ustr = params.get("u", sys.me.id());

        // 注册
        if (params.is("reg")) {
            NutMap hps = NutMap.NEW();
            hps.setv("tp", params.get("tp", "user"));
            hps.setv("ustr", ustr);
            AjaxReturn ar = httpPost(String.format(API_TMPL + API_REG, service, ts), hps);
            sys.out.println(Json.toJson(ar));
        }

        // 查询我的工单
        if (params.is("query")) {

        }

        // 提交/回复工单
        if (params.is("post")) {

        }

        // 添加附件
        if (params.is("atta")) {

        }
    }

    private AjaxReturn httpPost(String url, NutMap httpParams) {
        log.infof("ticket: httpPost access %s with params %s",
                  url,
                  Json.toJson(httpParams, JsonFormat.compact()));
        Response response = Http.post2(url, httpParams, 30000);
        if (response.isOK()) {
            String rcontent = response.getContent();
            return Json.fromJson(AjaxReturn.class, rcontent);
        }
        return Ajax.fail().setMsg(response.getDetail());
    }

}