package org.nutz.walnut.ext.sys.noti.hdl;

import java.util.ArrayList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Region;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.sys.noti.WnNotiHandler;
import org.nutz.walnut.ext.sys.noti.WnNotis;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public class noti_send implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {

        // 分析参数
        int limit = hc.params.getInt("limit", 0);
        long timeout = hc.params.getLong("timeout", 10) * 1000;

        // 准备记录发送结果
        final List<WnObj> reList = new ArrayList<WnObj>(limit);

        // 进入内核态执行发送
        Wn.WC().core(null, true, null, () -> {

            // 得到要操作的用户
            WnAccount me = sys.getMe();

            // 得到自己在 root 组的权限
            boolean I_am_member_of_root = sys.auth.isMemberOfGroup(me, "root");

            // 得到消息主目录
            WnObj oNotiHome = sys.io.createIfNoExists(null, "/sys/noti", WnRace.DIR);

            // 准备查询条件
            WnQuery q = Wn.Q.pid(oNotiHome);
            q.setv("tp", "wn_noti");
            q.setv("noti_st", WnNotis.TP_WAITING);

            // 普通用户只能主动发送自己发的消息
            if (!I_am_member_of_root) {
                q.setv("noti_c", me.getName());
            }

            // 按照优先级排序，先进入的消息，先发送
            q.desc("noti_lv").asc("ct");

            // 执行发送
            __do_send_in_loop(sys, limit, timeout, q, reList);
        });

        // 输出结果
        if (!hc.params.is("Q")) {
            // 输出成 JSON
            if (hc.params.is("json")) {
                sys.out.println(Json.toJson(reList, hc.jfmt));
            }
            // 仅仅打印消息 ID
            else {
                for (WnObj oN : reList) {
                    String prefix = "OK";
                    switch (oN.getInt("noti_st", WnNotis.TP_FAIL)) {
                    case WnNotis.TP_FAIL:
                        prefix = "KO";
                        break;
                    case WnNotis.TP_DONE:
                        prefix = "OK";
                        break;
                    default:
                        prefix = "!!";
                    }
                    sys.out.printlnf("%s [%s]%s >> %s:%s",
                                     prefix,
                                     oN.getString("noti_type"),
                                     oN.id(),
                                     oN.getString("noti_target"),
                                     oN.getString("noti_errmsg", ""));
                }
            }
        }

    }

    private void __do_send_in_loop(WnSystem sys,
                                   int limit,
                                   long timeout,
                                   WnQuery q,
                                   List<WnObj> reList) {
        int i = 0;
        // 循环发送
        while (i++ < limit || limit <= 0) {
            // 得到当前系统时间
            long nowInMs = Wn.now();

            // 更新查询时间
            q.setv("noti_timeout_at", Region.Longf("(,%d)", nowInMs));

            // 首先从队列里获取一个消息，同时标识上 timeout_at
            WnObj oN = sys.io.setBy(q, "noti_timeout_at", nowInMs + timeout, true);

            // 木有消息了 ...
            if (null == oN)
                break;

            // 得到消息的类型
            String notiType = oN.getString("noti_type");

            // 调用处理器发送
            try {
                WnNotiHandler noti = WnNotis.checkHandler(notiType);

                // 得到之前的重试次数
                int retry = oN.getInt("noti_retry", 0);

                // 一直尝试到最大重试次数
                int maxRetry = oN.getInt("noti_retry_max", 3);

                // 记录最后一次失败原因
                String errmsg = retry < maxRetry ? null : "e.cmd.noti.send.reachMaxRetry";

                // 持续重试
                if (null == errmsg) {
                    while (retry < maxRetry) {
                        errmsg = Strings.trim(noti.send(sys, oN));
                        // 失败了，休息一下 CPU，马上重试
                        if (!Strings.isEmpty(errmsg)) {
                            retry = sys.io.inc(oN.id(), "noti_retry", 1, true);
                            Thread.sleep(1);
                            continue;
                        }
                        // 成功了，那么标记一下发送状态，退出循环
                        else {
                            oN.setv("noti_st", WnNotis.TP_DONE);
                            sys.io.set(oN, "^noti_st$");
                            errmsg = null;
                            break;
                        }
                    }
                }

                // 失败的话，标记失败原因
                if (!Strings.isEmpty(errmsg)) {
                    oN.setv("noti_st", WnNotis.TP_FAIL);
                    oN.setv("noti_errmsg", Strings.sBlank(errmsg, "e.unknown"));
                    sys.io.set(oN, "^noti_(st|errmsg)$");
                }

            }
            // 处理中的任何未捕捉的错误，都会导致消息处理错误，做了标记以便不再处理
            catch (Exception e) {
                oN.setv("noti_st", WnNotis.TP_INVALID);
                oN.setv("noti_errmsg", Strings.sBlank(e.toString(), "e.unknown"));
                sys.io.set(oN, "^noti_(st|errmsg)$");
            }

            // 将消息对象，计入返回结果
            reList.add(oN);
        }
    }

}
