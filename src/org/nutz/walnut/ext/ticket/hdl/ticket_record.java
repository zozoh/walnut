package org.nutz.walnut.ext.ticket.hdl;

import java.util.ArrayList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnPager;
import org.nutz.walnut.util.ZParams;

public class ticket_record implements JvmHdl {

    private Log log = Logs.get();

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        ZParams params = ZParams.parse(hc.args, null);
        String ustr = params.get("u", sys.me.id());

        NutMap myConf = NutMap.NEW();
        myConf.setv("tp", "cservice");
        myConf.setv("ts", "");
        myConf.setv("tsNm", sys.getHome().name());
        // 非域用户，切必须是客服
        if (params.is("conf")) {
            WnObj myConfObj = myConf(sys);
            if (myConfObj == null) {
                sys.err.println("e.ticket: init file ~/.ticket_my.json, if you want to edit it, exec 'open wedit ~/.ticket_my.json'");
                return;
            } else {
                myConf = sys.io.readJson(myConfObj, NutMap.class);
                // 防止域用户用了conf，如果发现是自己的域则改成
                if (sys.getHome().name().equals(myConf.getString("ts"))) {
                    myConf.setv("ts", "");
                }
            }
        }

        String ts = params.get("ts", myConf.getString("ts"));
        String tp = params.get("tp", myConf.getString("tp"));
        boolean isUser = "user".equals(tp);

        String thString = Strings.isBlank(ts) ? "~/.ticket" : "~/.ticket_" + ts;
        WnObj ticketHome = sys.io.fetch(null, Wn.normalizeFullPath(thString, sys));
        if (ticketHome == null) {
            sys.err.printf("e.ticket: data dir [%s] not found, please exec 'ticket init'",
                           thString);
            return;
        }

        WnObj tPeople = findTicketPeople(sys, ticketHome, isUser, ustr);
        if (tPeople == null) {
            sys.err.printf("e.ticket: ticket people [%s] not found, please reg the walnut user",
                           ustr);
            return;
        }
        WnObj recordDir = sys.io.check(ticketHome, "record");

        NutMap sort = null;
        if (params.has("sort") && !Strings.isBlank(params.getString("sort", ""))) {
            sort = Lang.map(params.check("sort"));
        }

        // 查询全局工单
        if (params.has("search")) {
            params.setv("pager", true);
            // 仅限客服
            if (isUser) {
                sys.err.printf("e.ticket: cservice can serach record, others only query their records");
                return;
            }
            NutMap qvars = Lang.map(params.getString("search"));
            WnQuery wnQuery = new WnQuery();
            wnQuery.add(qvars);
            wnQuery.setv("pid", recordDir.id());
            wnQuery.sortBy("ct", -1);
            wnQuery.sortBy("nm", 1);

            WnPager wp = new WnPager(params);
            wp.setupQuery(sys, wnQuery);

            if (sort != null) {
                wnQuery.sort(sort);
            }

            List<WnObj> pList = sys.io.query(wnQuery);
            sys.out.println(outList(pList, wp));
        }

        // 查询与我相关工单
        if (params.has("query")) {
            params.setv("pager", true);
            NutMap qvars = Lang.map(params.getString("query"));
            WnQuery wnQuery = new WnQuery();
            wnQuery.add(qvars);
            if (isUser) {
                wnQuery.setv("usrId", tPeople.getString("usrId"));
            } else {
                wnQuery.setv("csId", tPeople.getString("usrId"));
            }
            wnQuery.setv("pid", recordDir.id());
            wnQuery.sortBy("ct", -1);
            wnQuery.sortBy("nm", 1);

            WnPager wp = new WnPager(params);
            wp.setupQuery(sys, wnQuery);

            if (sort != null) {
                wnQuery.sort(sort);
            }

            List<WnObj> pList = sys.io.query(wnQuery);
            sys.out.println(outList(pList, wp));
        }

        // 新工单
        if (params.is("new")) {
            // 仅限用户
            if (!isUser) {
                sys.err.printf("e.ticket: record can't create by cservice");
                return;
            }
            // 组装工单内容
            NutMap tMeta = Lang.map(params.getString("c"));
            tMeta.setv("usrId", tPeople.getString("usrId"));
            tMeta.setv("tickerStart", System.currentTimeMillis());
            tMeta.setv("ticketEnd", -1);
            tMeta.setv("ticketStatus", "new");
            tMeta.setv("ticketStep", "1");
            tMeta.setv("ticketTp", tMeta.get("ticketTp", "question"));
            tMeta.setv("lbls", new String[0]);
            tMeta.setv("ticketIssue", new String[0]);
            tMeta.setv("request", new NutMap[0]);

            // 写入对象
            WnObj reObj = sys.io.create(recordDir,
                                        tPeople.name() + "_" + System.currentTimeMillis(),
                                        WnRace.DIR);
            sys.io.appendMeta(reObj, tMeta);
            // 返回工单全部内容
            sys.out.printf(Json.toJson(sys.io.get(reObj.id())));
        }

        // 分配/转移客服, 仅限客服
        if (params.has("assign")) {
            // 仅限客服
            if (isUser) {
                sys.err.printf("e.ticket: cservice can assign record, others only query their records");
                return;
            }
            // 获取工单号
            String rid = params.getString("assign");
            WnObj curRecord = getRecord(sys, rid);
            if (curRecord != null) {
                String auser = params.get("tu", ustr);
                WnObj csPeople = findTicketPeople(sys, ticketHome, false, auser);
                if (csPeople == null) {
                    sys.err.printf("e.ticket: cservice [%s] not found, check param 'tu'", auser);
                    return;
                }
                // 判断是否已经是当前用户了
                if (curRecord.containsKey("csId")
                    && curRecord.getString("csId").equals(csPeople.getString("usrId"))) {
                    sys.err.printf("e.ticket: current record csId is [%s], no need to assign again",
                                   auser);
                    return;
                }
                // 已经分配需要记录到历史中
                curRecord.setv("ticketStatus", "assign");
                if (curRecord.containsKey("csId")) {
                    curRecord.addv2("csTransTime", System.currentTimeMillis());
                    curRecord.addv2("csTrans", curRecord.getString("csId"));
                    curRecord.setv("ticketStatus", "reassign");
                }
                curRecord.setv("ticketStep", "2");
                curRecord.setv("csId", csPeople.getString("usrId"));
                curRecord.setv("csAlias", csPeople.getString("usrAlias"));
                sys.io.appendMeta(curRecord,
                                  "^csId|csAlias|csTrans|csTransTime|ticketStatus|ticketStep$");
                sys.out.print(Json.toJson(curRecord.toMap("^id|csId|csAlias$")));

                // 通知客服 工单分配给你了
                notiWSClient(sys,
                             csPeople,
                             NutMap.NEW()
                                   .setv("action", "noti")
                                   .setv("tp", "assign")
                                   .setv("title",
                                         "工单[" + curRecord.getString("text") + "]分配给你了，请尽快处理"));
                // 通知用户 工单已分配
                WnObj crUsr = findTicketPeople(sys, ticketHome, true, curRecord.getString("usrId"));
                notiWSClient(sys,
                             crUsr,
                             NutMap.NEW()
                                   .setv("action", "noti")
                                   .setv("tp", "assign")
                                   .setv("title",
                                         "工单["
                                                  + curRecord.getString("text")
                                                  + "]已分配给客服("
                                                  + tPeople.getString("csAlias")
                                                  + ")"));
            } else {
                sys.err.printf("e.ticket: record[%s] not found", rid);
            }
        }

        // 获取工单
        if (params.has("fetch")) {
            // 获取工单号
            String rid = params.getString("fetch");
            WnObj curRecord = getRecord(sys, rid);
            if (curRecord != null) {
                if (isUser) {
                    // 先检查是否我的订单
                    if (!tPeople.getString("usrId").equals(curRecord.getString("usrId"))) {
                        sys.err.printf("e.ticket: record[%s] current user is not you",
                                       curRecord.id());
                        return;
                    }
                } else {
                    // 先检查是否是分配给我的订单
                    if (!tPeople.getString("usrId").equals(curRecord.getString("csId"))) {
                        sys.err.printf("e.ticket: record[%s] current cservice is you",
                                       curRecord.id());
                        return;
                    }
                }
                // 是我的，可以返回内容了
                sys.out.print(Json.toJson(curRecord));
            } else {
                sys.err.printf("e.ticket: record[%s] not found", rid);
            }
        }

        // 回复工单
        if (params.has("reply")) {
            // 获取工单号
            String rid = params.getString("reply");
            boolean isMeta = params.is("m", false);
            boolean isOpen = params.is("open", false);
            boolean isClose = params.is("close", false);
            int editIndex = params.getInt("edit", -1);
            int deletIndex = params.getInt("delete", -1);
            WnObj curRecord = getRecord(sys, rid);
            if (curRecord != null) {
                // 只更新meta
                if (isMeta) {
                    NutMap meta = Lang.map(params.getString("c"));
                    sys.io.appendMeta(curRecord, meta);
                    sys.out.print(Json.toJson(getRecord(sys, rid)));
                }
                // 打开
                else if (isOpen) {
                    NutMap meta = NutMap.NEW();
                    meta.setv("ticketStatus", "reassign"); // 算是重新指派给客服了
                    meta.setv("ticketEnd", -1);
                    meta.setv("ticketStep", "2");
                    sys.io.appendMeta(curRecord, meta);
                    sys.out.print(Json.toJson(getRecord(sys, rid)));
                }
                // 其他内容
                else {
                    if (isUser) {
                        // 先检查是否我的订单
                        if (tPeople.getString("usrId").equals(curRecord.getString("usrId"))) {
                            // 关闭
                            if (isClose) {
                                curRecord.setv("ticketStatus", "done");
                                curRecord.setv("ticketEnd", System.currentTimeMillis());
                                curRecord.setv("ticketStep", "3");
                            }
                            // 删除某条
                            if (deletIndex > -1) {
                                List<NutMap> requestList = curRecord.getAsList("request",
                                                                               NutMap.class);
                                requestList.remove(deletIndex);
                                curRecord.setv("request", requestList);
                                curRecord.setv("ticketStatus", "ureply");
                            }
                            NutMap ureply = Lang.map(params.getString("c"));
                            boolean hasContent = !Strings.isBlank(ureply.getString("text", ""));
                            boolean hasAtta = params.has("atta");
                            // 有内容提交
                            if (hasContent || hasAtta) {
                                // 修改旧贴
                                if (editIndex > -1) {
                                    List<NutMap> requestList = curRecord.getAsList("request",
                                                                                   NutMap.class);
                                    NutMap editreplay = requestList.get(editIndex);
                                    editreplay.setv("text", ureply.getString("text", ""));
                                    editreplay.setv("attachments", attas(sys, curRecord, params));
                                }
                                // 提交新贴
                                else {
                                    ureply.setv("attachments", attas(sys, curRecord, params));
                                    ureply.setv("time", System.currentTimeMillis());
                                    curRecord.addv2("request", ureply);
                                }
                                curRecord.setv("ticketStatus", "ureply");
                            }
                            // 如果没有客服接这个任务
                            if (!curRecord.has("csId")) {
                                curRecord.setv("ticketStatus", "new");
                            }
                            sys.io.appendMeta(curRecord,
                                              "^request|ticketStatus|ticketStep|ticketEnd$");
                            sys.out.print(Json.toJson(curRecord));
                        } else {
                            sys.err.printf("e.ticket: record[%s] current user is not you",
                                           curRecord.id());
                        }
                    } else {
                        // 先检查是否是分配给我的订单
                        if (tPeople.getString("usrId").equals(curRecord.getString("csId"))) {
                            // 关闭
                            if (isClose) {
                                curRecord.setv("ticketStatus", "done");
                                curRecord.setv("ticketEnd", System.currentTimeMillis());
                                curRecord.setv("ticketStep", "3");
                            }
                            // 删除某条
                            if (deletIndex > -1) {
                                List<NutMap> respList = curRecord.getAsList("response",
                                                                            NutMap.class);
                                respList.remove(deletIndex);
                                curRecord.setv("response", respList);
                                curRecord.setv("ticketStatus", "creply");
                            }
                            NutMap creply = Lang.map(params.getString("c"));
                            boolean hasContent = !Strings.isBlank(creply.getString("text", ""));
                            boolean hasAtta = params.has("atta");
                            // 有内容提交
                            if (hasContent || hasAtta) {
                                // 修改旧贴
                                if (editIndex > -1) {
                                    List<NutMap> respList = curRecord.getAsList("response",
                                                                                NutMap.class);
                                    NutMap editreplay = respList.get(editIndex);
                                    editreplay.setv("text", creply.getString("text", ""));
                                    editreplay.setv("attachments", attas(sys, curRecord, params));
                                }
                                // 提交新贴
                                else {
                                    creply.setv("csId", tPeople.getString("usrId"));
                                    creply.setv("csAlias", tPeople.getString("usrAlias"));
                                    creply.setv("attachments", attas(sys, curRecord, params));
                                    creply.setv("time", System.currentTimeMillis());
                                    curRecord.addv2("response", creply);

                                }
                                curRecord.setv("ticketStatus", "creply");
                            }
                            sys.io.appendMeta(curRecord,
                                              "^response|ticketStatus|ticketStep|ticketEnd$");
                            sys.out.print(Json.toJson(curRecord));
                        } else {
                            sys.err.printf("e.ticket: record[%s] current cservice is you",
                                           curRecord.id());
                        }
                    }
                }
            } else {
                sys.err.printf("e.ticket: record[%s] not found", rid);
            }
        }

    }

    private List<NutMap> attas(WnSystem sys, WnObj curRecord, ZParams params) {
        List<NutMap> attas = new ArrayList<>();
        if (params.has("atta")) {
            String destDir = curRecord.getRegularPath();
            String[] fids = params.get("atta").split(",");
            for (String afid : fids) {
                WnObj attaFile = sys.io.get(afid);
                sys.io.move(attaFile, destDir);
                attas.add(NutMap.NEW()
                                .setv("ph", attaFile.path())
                                .setv("id", attaFile.id())
                                .setv("nm", attaFile.name())
                                .setv("tp", attaFile.type()));
            }
        }
        return attas;
    }

    private WnObj getRecord(WnSystem sys, String rid) {
        WnObj record = sys.io.get(rid);
        if (record == null) {
            sys.err.printf("e.ticket: not find ticket by id[%s]", rid);
        }
        return record;
    }

    private WnObj findTicketPeople(WnSystem sys, WnObj ticketHome, boolean isUser, String ustr) {
        WnObj tp = sys.io.fetch(ticketHome, (isUser ? "user" : "cservice") + "/wn_" + ustr);
        if (tp != null) {
            tp.setv("isUser", isUser);
        }
        return tp;
    }

    private WnObj myConf(WnSystem sys) {
        WnObj myHome = sys.getHome();
        return sys.io.fetch(myHome, ".ticket_my.json");
    }

    private String outList(List<WnObj> outs, WnPager wp) {
        NutMap re = new NutMap();
        re.setv("list", outs);
        re.setv("pager",
                Lang.mapf("pn:%d,pgsz:%d,pgnb:%d,sum:%d,skip:%d,nb:%d",
                          wp.pn,
                          wp.pgsz,
                          wp.sum_page,
                          wp.sum_count,
                          wp.skip,
                          outs.size()));
        return Json.toJson(re);
    }

    private void notiWSClient(WnSystem sys, WnObj toUser, NutMap content) {
        String fid = toUser.id();
        String cJson = Json.toJson(content, JsonFormat.compact());
        String cmd = String.format("websocket text id:%s '%s'", fid, cJson);
        log.infof("ticket noti-ws: %s", cmd);
        sys.exec(cmd);
    }

}