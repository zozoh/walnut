package org.nutz.walnut.ext.ticket.hdl;

import org.nutz.castor.Castors;
import org.nutz.http.Http;
import org.nutz.http.Response;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;
import org.nutz.web.ajax.Ajax;
import org.nutz.web.ajax.AjaxReturn;

import com.beust.jcommander.Strings;

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
    // 注册
    private static String API_REG = "/ticket/reg";
    // 提交、回复
    private static String API_POST = "/ticket/post";
    // 查询
    private static String API_QUERY = "/ticket/query";
    // 获取
    private static String API_FETCH = "/ticket/fetch";

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        ZParams params = ZParams.parse(hc.args, null);

        WnObj myConfObj = myConf(sys);
        NutMap myConf = sys.io.readJson(myConfObj, NutMap.class);

        String service = params.get("s", myConf.getString("s"));
        String ts = params.get("ts", myConf.getString("ts"));
        String tp = params.get("tp", myConf.getString("tp"));
        String ustr = params.get("u", sys.me.id());

        // 查看/切换配置
        if (params.has("conf")) {
            String confNm = params.getString("conf", "");
            if (confNm.equals("true") || Strings.isStringEmpty(confNm)) {
                myConf.remove("confs");
                sys.out.print(Json.toJson(myConf));
            } else if (confNm.equals("list")) {
                NutMap confs = myConf.getAs("confs", NutMap.class);
                if (confs.isEmpty()) {
                    sys.out.println("no conf found");
                } else {
                    int i = 1;
                    for (String tcnm : confs.keySet()) {
                        NutMap tconf = confs.getAs(tcnm, NutMap.class);
                        sys.out.printlnf("%d. %s[%s]",
                                         i++,
                                         tconf.getString("tsNm"),
                                         tconf.getString("ts"));
                    }
                }
            } else {
                NutMap confs = myConf.getAs("confs", NutMap.class);
                if (confs.has(confNm)) {
                    NutMap nconf = confs.getAs(confNm, NutMap.class);
                    myConf.mergeWith(nconf);
                    sys.io.writeJson(myConfObj, myConf, JsonFormat.forLook());
                    myConf.remove("confs");
                    sys.out.printf("# switch conf to [%s]\n%s",
                                   nconf.getString("tsNm"),
                                   Json.toJson(myConf));
                } else {
                    sys.err.printf("ticket: not find conf named [%s], exec 'ticket my -conf list' to check the list",
                                   confNm);
                }
            }
            return;
        }

        // 组装请求参数
        NutMap httpPs = NutMap.NEW();
        httpPs.setv("tp", tp);
        httpPs.setv("ustr", ustr);

        // 注册
        if (params.is("reg")) {
            AjaxReturn ar = httpPost(String.format(API_TMPL + API_REG, service, ts), httpPs);
            if (ar.isOk()) {
                // 记录配置到本地
                NutMap regReply = Castors.me().castTo(ar.getData(), NutMap.class);
                sys.out.printf("%s\n", regReply.getString("welcome"));

                // 记录到本地并切换
                NutMap regConf = NutMap.NEW();
                regConf.setv("s", service);
                regConf.setv("tp", tp);
                regConf.setv("ts", ts);
                regConf.setv("tsNm", regReply.getString("nm"));

                NutMap confs = myConf.getAs("confs", NutMap.class);
                confs.setv(ts, regConf);

                myConf.mergeWith(regConf);
                myConf.setv("confs", confs);

                sys.io.writeJson(myConfObj, myConf, JsonFormat.forLook());

                sys.exec("ticket my -conf " + ts);
            } else {
                sys.out.println(Json.toJson(ar));
            }
        }

        // 查询我的工单
        else if (params.has("query")) {

        }

        // 获取指定工单
        else if (params.has("fetch")) {

        }

        // 提交/回复工单
        else if (params.has("post")) {

        }

    }

    private WnObj myConf(WnSystem sys) {
        WnObj myHome = sys.getHome();
        WnObj myConfObj = sys.io.fetch(myHome, ".ticket_my.json");
        if (myConfObj == null) {
            sys.out.println("ticket: init file ~/.ticket_my.json, if you want to edit it, exec 'open wedit ~/.ticket_my.json'");
            myConfObj = sys.io.create(myHome, ".ticket_my.json", WnRace.FILE);
            NutMap myConf = NutMap.NEW();
            myConf.setv("s", "127.0.0.1");
            myConf.setv("tp", "user");
            myConf.setv("ts", sys.getHome().name());
            myConf.setv("tsNm", sys.getHome().name());
            myConf.setv("confs", NutMap.NEW());
            sys.io.writeJson(myConfObj, myConf, JsonFormat.forLook());
        }
        return myConfObj;
    }

    private AjaxReturn httpPost(String url, NutMap httpParams) {
        log.infof("ticket: regapi access %s with params %s",
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