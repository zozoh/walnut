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
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

import com.beust.jcommander.Strings;

public class ticket_record implements JvmHdl {

    private Log log = Logs.get();

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        ZParams params = ZParams.parse(hc.args, null);
        String ustr = params.get("u", sys.me.id());
        boolean isUser = "user".equals(params.get("tp", "cservice"));
        String ts = params.get("ts", "");
        String thString = Strings.isStringEmpty(ts) ? "~/.ticket" : "~/.ticket_" + ts;
        WnObj ticketHome = sys.io.fetch(null, Wn.normalizeFullPath(thString, sys));
        if (ticketHome == null) {
            sys.err.printf("ticket: data dir [%s] not found, please exec 'ticket init'", thString);
            return;
        }

        WnObj tPeople = findTicketPeople(sys, ticketHome, isUser, ustr);
        if (tPeople == null) {
            sys.err.printf("ticket: ticket people [%s] not found, please reg the walnut user",
                           ustr);
            return;
        }
        WnObj recordDir = sys.io.check(ticketHome, "record");

        // 查询全局工单, 仅限客服
        if (params.has("search") && !isUser) {
            NutMap qvars = Lang.map(params.getString("search"));
            WnQuery wnQuery = new WnQuery();
            wnQuery.add(qvars);
            wnQuery.setv("pid", recordDir.id());
            wnQuery.sortBy("ct", -1);
            wnQuery.sortBy("nm", 1);
            List<WnObj> pList = sys.io.query(wnQuery);
            sys.out.println(Json.toJson(pList));
        }

        // 查询与我相关工单
        if (params.has("query")) {
            NutMap qvars = Lang.map(params.getString("query"));
            WnQuery wnQuery = new WnQuery();
            wnQuery.add(qvars);
            if (isUser) {
                wnQuery.setv("usrId", tPeople.id());
            } else {
                wnQuery.setv("csId", tPeople.id());
            }
            wnQuery.setv("pid", recordDir.id());
            wnQuery.sortBy("ct", -1);
            wnQuery.sortBy("nm", 1);
            List<WnObj> pList = sys.io.query(wnQuery);
            sys.out.println(Json.toJson(pList));
        }

        // 新工单,仅限用户
        if (params.has("new") && isUser) {
            // 组装工单内容
            NutMap tMeta = Lang.map(params.getString("new"));
            if (!tMeta.containsKey("title")) {
                sys.err.print("ticket: new record need [title]");
                return;
            }
            tMeta.setv("tickerStart", System.currentTimeMillis());
            tMeta.setv("ticketEnd", -1);
            tMeta.setv("ticketStatus", "new");
            tMeta.setv("ticketTp", tMeta.get("ticketTp", "question"));
            tMeta.setv("lbls", new String[0]);
            tMeta.setv("ticketIssue", new String[0]);

            String text = tMeta.getString("text", "");
            NutMap req = NutMap.NEW();
            req.setv("text", text);
            req.setv("attachments", new String[0]);
            req.setv("time", tMeta.getLong("tickerStart"));
            tMeta.addv2("request", req);

            // 写入对象
            WnObj reObj = sys.io.create(recordDir,
                                        tPeople.name() + "_" + System.currentTimeMillis(),
                                        WnRace.DIR);
            sys.io.appendMeta(reObj, tMeta);
            // 返回工单ID
            sys.out.printf("{ id: '%s'}", reObj.id());
        }

        // 分配/转移客服, 仅限客服
        if (params.has("assign") && !isUser) {
            // 获取工单号
            String rid = params.getString("rid");
            WnObj curRecord = getRecord(sys, rid);
            if (curRecord != null) {
                String auser = params.get("assign", sys.me.id());
                WnObj csPeople = findTicketPeople(sys, ticketHome, false, auser);
                // 已经分配需要记录到历史中
                curRecord.setv("ticketStatus", "assign");
                if (curRecord.containsKey("csId")) {
                    curRecord.addv2("csTransTime", System.currentTimeMillis());
                    curRecord.addv2("csTrans", curRecord.getString("csId"));
                    curRecord.setv("ticketStatus", "reassign");
                }
                curRecord.setv("csId", csPeople.getString("usrId"));
                curRecord.setv("csAlias", csPeople.getString("usrAlias"));
                sys.io.appendMeta(curRecord, "^csId|csAlias|csTrans|csTransTime|ticketStatus$");
            }
            sys.out.print(Json.toJson(curRecord.toMap("^id|csId|csAlias$")));
        }

        // 回复工单
        if (params.has("reply")) {
            // 获取工单号
            String rid = params.getString("rid");
            WnObj curRecord = getRecord(sys, rid);
            if (curRecord != null) {
                if (isUser) {
                    NutMap ureply = Lang.map(params.getString("reply"));
                    ureply.setv("attachments", new String[0]);
                    ureply.setv("time", System.currentTimeMillis());
                    curRecord.addv2("request", ureply);
                    curRecord.setv("ticketStatus", "ureply");
                    sys.io.appendMeta(curRecord, "^request|ticketStatus$");
                    sys.out.print(Json.toJson(curRecord.toMap("^id|ticketStatus|lm$")));
                } else {
                    // 先检查是否是分配给我的订单
                    if (tPeople.getString("usrId").equals(curRecord.getString("csId"))) {
                        NutMap creply = Lang.map(params.getString("reply"));
                        creply.setv("csId", tPeople.getString("usrId"));
                        creply.setv("csAlias", tPeople.getString("usrAlias"));
                        creply.setv("attachments", new String[0]);
                        creply.setv("time", System.currentTimeMillis());
                        curRecord.addv2("response", creply);
                        curRecord.setv("ticketStatus", "creply");
                        sys.io.appendMeta(curRecord, "^request|ticketStatus$");
                        sys.out.print(Json.toJson(curRecord.toMap("^id|ticketStatus|lm$")));
                    } else {
                        sys.err.printf("ticket: record[%s] current cservice is not [%s]",
                                       curRecord.id(),
                                       curRecord.getString("csAlias"));
                    }
                }

            }
        }

    }

    private WnObj getRecord(WnSystem sys, String rid) {
        WnObj record = sys.io.get(rid);
        if (record == null) {
            sys.err.printf("ticket: not find ticket by id[%s]", rid);
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

}