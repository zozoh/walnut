package org.nutz.walnut.ext.ticket.hdl;

import java.io.InputStream;

import org.nutz.castor.Castors;
import org.nutz.http.Http;
import org.nutz.http.Response;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
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
    // 我的查询
    private static String API_QUERY = "/ticket/query";
    // 全局查询
    private static String API_SEARCH = "/ticket/search";
    // 获取
    private static String API_FETCH = "/ticket/fetch";
    // 分配
    private static String API_ASSIGN = "/ticket/assign";

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        ZParams params = ZParams.parse(hc.args, null);

        WnObj myConfObj = myConf(sys);
        NutMap myConf = sys.io.readJson(myConfObj, NutMap.class);

        String ustr = params.get("u", sys.me.id());
        String service = params.get("s", myConf.getString("s"));
        String ts = params.get("ts", myConf.getString("ts"));
        String tp = params.get("tp", myConf.getString("tp"));

        // 查看/切换配置
        if (params.has("conf")) {
            String confNm = params.getString("conf", "");
            if (confNm.equalsIgnoreCase("true")
                || confNm.equalsIgnoreCase("false")
                || Strings.isBlank(confNm)) {
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
                    sys.err.printf("e.ticket: not find conf named [%s], exec 'ticket my -conf list' to check the list",
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
            AjaxReturn ar = httpPost(String.format(API_TMPL + API_REG, service, ts), httpPs, null);
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
                sys.err.println(Json.toJson(ar));
            }
        }

        // 查询我的工单
        else if (params.has("list")) {
            httpPs.setv("query", params.getString("list"));
            httpPs.setv("skip", params.getInt("skip", 0));
            httpPs.setv("limit", params.getInt("limit", 10));
            AjaxReturn ar = httpPost(String.format(API_TMPL + API_QUERY, service, ts),
                                     httpPs,
                                     null);
            if (ar.isOk()) {
                sys.out.print(Json.toJson(ar.getData())); // 只返回内容
            } else {
                sys.err.println(Json.toJson(ar));
            }
        }

        // 查询全局工单
        else if (params.has("search")) {
            httpPs.setv("search", params.getString("search"));
            httpPs.setv("skip", params.getInt("skip", 0));
            httpPs.setv("limit", params.getInt("limit", 10));
            AjaxReturn ar = httpPost(String.format(API_TMPL + API_SEARCH, service, ts),
                                     httpPs,
                                     null);
            if (ar.isOk()) {
                sys.out.print(Json.toJson(ar.getData())); // 只返回内容
            } else {
                sys.err.println(Json.toJson(ar));
            }
        }

        // 分配
        else if (params.has("assign")) {
            httpPs.setv("assign", params.getString("assign"));
            httpPs.setv("tu", params.getString("tu", ustr));
            AjaxReturn ar = httpPost(String.format(API_TMPL + API_ASSIGN, service, ts),
                                     httpPs,
                                     null);
            if (ar.isOk()) {
                sys.out.print(Json.toJson(ar));
            } else {
                sys.err.println(Json.toJson(ar));
            }
        }

        // 获取指定工单
        else if (params.has("fetch")) {
            httpPs.setv("fetch", params.getString("fetch"));
            AjaxReturn ar = httpPost(String.format(API_TMPL + API_FETCH, service, ts),
                                     httpPs,
                                     null);
            if (ar.isOk()) {
                sys.out.print(Json.toJson(ar));
            } else {
                sys.err.println(Json.toJson(ar));
            }
        }

        // 提交/回复工单
        else if (params.has("post")) {
            String trid = null;
            String pcontent = params.getString("c", "");
            NutMap content = null;
            if (!params.getString("post").equalsIgnoreCase("true")
                && !params.getString("post").equalsIgnoreCase("false")
                && !Strings.isBlank(params.getString("post"))) {
                trid = params.getString("post");
            }
            if (!Strings.isBlank(pcontent)) {
                content = Lang.map(params.get("c"));
            }
            // 新工单
            if (trid == null) {
                // 检查content
                if (content == null || !content.containsKey("text")) {
                    sys.err.print("e.ticket: post ticket need content, -c 'text: \"desc....\"'");
                    return;
                }
            }
            // 已存在工单
            else {
                httpPs.setv("rid", trid);
            }
            if (content != null) {
                httpPs.setv("content", Json.toJson(content, JsonFormat.compact()));
            }

            // 附件
            InputStream attaFileIn = null;
            if (params.has("atta")) {
                httpPs.setv("atta", true);
                String fids = params.getString("atta");
                // 从管道中读取
                if (fids.equalsIgnoreCase("true") || fids.equalsIgnoreCase("false")) {
                    attaFileIn = sys.in.getInputStream();
                }
                // 文件id列表
                else {
                    httpPs.setv("fids", fids);
                }
            }

            AjaxReturn ar = httpPost(String.format(API_TMPL + API_POST, service, ts),
                                     httpPs,
                                     attaFileIn);
            if (ar.isOk()) {
                sys.out.print(Json.toJson(ar));
            } else {
                sys.err.println(Json.toJson(ar));
            }
        }

    }

    private WnObj myConf(WnSystem sys) {
        WnObj myHome = sys.getHome();
        WnObj myConfObj = sys.io.fetch(myHome, ".ticket_my.json");
        if (myConfObj == null) {
            sys.out.println("init file ~/.ticket_my.json, if you want to edit it, exec 'open wedit ~/.ticket_my.json'");
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

    private AjaxReturn httpPost(String url, NutMap httpParams, InputStream attaFileIn) {
        log.infof("regapi access %s with params %s",
                  url,
                  Json.toJson(httpParams, JsonFormat.compact()));
        if (attaFileIn != null) {
            // TODO
        }

        Response response = Http.post2(url, httpParams, 30000);
        if (response.isOK()) {
            String rcontent = response.getContent();
            return Json.fromJson(AjaxReturn.class, rcontent);
        }
        return Ajax.fail().setMsg(response.getDetail());
    }

}