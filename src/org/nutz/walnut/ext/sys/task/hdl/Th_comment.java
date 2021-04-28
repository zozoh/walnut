package org.nutz.walnut.ext.sys.task.hdl;

import java.util.ArrayList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.lang.Times;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.sys.task.TaskCtx;
import org.nutz.walnut.ext.sys.task.TaskHdl;
import org.nutz.walnut.impl.box.LinuxTerminal;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public class Th_comment implements TaskHdl {

    @Override
    public void invoke(WnSystem sys, TaskCtx sc) throws Exception {
        // 分析参数
        ZParams params = ZParams.parse(sc.args, "^json|nocolor$");

        // 如果没有参数，则表示要查询
        if (params.vals.length == 0) {
            // 必须指定一个任务
            if (null == sc.oTask)
                throw Er.create("e.cmd.task.notask");
            WnObj oCmtHome = sys.io.fetch(sc.oTask, "comments");
            __print_all_comments(sys, sc, params, oCmtHome);
            return;
        }

        // 参数不能少于两个
        if (params.vals.length < 2) {
            throw Er.create("e.cmd.task.lackargs", sc.args);
        }

        // 根据第一个参数决定要干什么
        String cmtType = params.vals[0];

        // 添加注释
        if ("add".equals(cmtType)) {
            // 必须指定一个任务
            if (null == sc.oTask)
                throw Er.create("e.cmd.task.notask");
            WnObj oCmtHome = sys.io.fetch(sc.oTask, "comments");
            __do_add(sys, sc, oCmtHome, params);
        }
        // 下面的操作，必须存在 comments 目录
        else {
            // 删除注释
            if ("del".equals(cmtType)) {
                WnObj oCmt = __check_cmt_obj(sys, sc, params.vals[1]);
                sys.io.delete(oCmt);
                __update_task_cmtnb(sys, sc, oCmt, -1);
            }
            // 修改注释
            else {
                String body = params.vals[1];
                WnObj oCmt = __check_cmt_obj(sys, sc, cmtType);
                __update_cmt_body(sys, oCmt, body);
            }
        }
    }

    private WnObj __check_cmt_obj(WnSystem sys, TaskCtx sc, String str) {
        // 根据ID
        if (str.startsWith("id:")) {
            return sys.io.checkById(str.substring(3));
        }
        // 根据名称
        if (null != sc.oTask) {
            WnObj oCmtHome = sys.io.fetch(sc.oTask, "comments");
            return sys.io.check(oCmtHome, str);
        }
        // 不合法
        throw Er.create("e.cmd.task.comment.noexists", str);
    }

    private void __do_add(WnSystem sys, TaskCtx sc, WnObj oCmtHome, ZParams params) {
        if (null == oCmtHome)
            oCmtHome = sys.io.createIfNoExists(sc.oTask, "comments", WnRace.DIR);
        String nm = Times.format("yyyyMMddHHmmssSSS", Times.now());
        WnObj oCmt = sys.io.create(oCmtHome, nm, WnRace.FILE);
        sys.io.set(oCmt.type("task_commant"), "^tp$");
        String body = params.vals[1];
        __update_cmt_body(sys, oCmt, body);
        __update_task_cmtnb(sys, sc, oCmt, 1);
    }

    private void __update_task_cmtnb(WnSystem sys, TaskCtx sc, WnObj oCmt, int inc) {
        // 找到任务
        if (null == sc.oTask) {
            sc.oTask = oCmt.parent().parent();
        }

        // 曾经有值，则仅仅增减
        if (sc.oTask.getInt("cmtnb") >= 0) {
            sys.io.inc(sc.oTask.id(), "cmtnb", inc, false);
        }

        // 否则要重新计算一下
        else {
            int nb = (int) sys.io.count(Wn.Q.pid(oCmt.parentId()));
            sc.oTask.setv("cmtnb", nb);
            sys.io.set(sc.oTask, "^cmtnb$");
        }
    }

    private void __update_cmt_body(WnSystem sys, WnObj oCmt, String body) {
        // 判断一下，如果内容长过 1024 个字符，写入文件内部
        if (body.length() > 1024) {
            sys.io.writeText(oCmt, body);
            if (oCmt.has("body")) {
                oCmt.setv("body", null);
                sys.io.set(oCmt, "^body$");
            }
        }
        // 否则将直接记入元数据，并丢弃原本的内容
        else {
            oCmt.setv("body", body);
            sys.io.set(oCmt, "^body$");
            if (oCmt.len() > 0)
                sys.io.writeText(oCmt, "");
        }
    }

    private void __print_all_comments(WnSystem sys, TaskCtx sc, ZParams params, WnObj oCmtHome) {
        List<WnObj> list;

        // 木有
        if (null == oCmtHome) {
            list = new ArrayList<WnObj>(0);
        }
        // 执行查询，并读取每个注释的详细内容
        else {
            WnQuery q = Wn.Q.pid(oCmtHome.id());
            q.asc("nm");
            list = sys.io.query(q);
            for (WnObj oCmt : list) {
                // 如果其元数据没有 body，则证明注释信息很多，已经存在了文件内容里
                String body = oCmt.getString("body");
                if (Strings.isBlank(body)) {
                    body = sys.io.readText(oCmt);
                    oCmt.setv("body", body);
                }
            }
        }

        // 输出 JSON
        if (params.is("json")) {
            sys.out.println(Json.toJson(list));
        }
        // 控制台输出
        else {
            boolean nocolor = params.is("nocolor");
            String sep = Strings.dup('-', 80);
            if (!nocolor) {
                sep = LinuxTerminal.wrapFont(sep, 2, 33);
            }
            for (WnObj oCmt : list) {
                String nm = oCmt.name();
                String c = oCmt.creator();
                String id = oCmt.id();
                String body = oCmt.getString("body");
                if (!nocolor) {
                    nm = LinuxTerminal.wrapFont(nm, 2, 33);
                    c = LinuxTerminal.wrapFont(c, 1, 32);
                    id = LinuxTerminal.wrapFont(id, 2, 37);
                }
                sys.out.printlnf("%s / %s @ %s:", id, nm, c);
                sys.out.println(body);
                sys.out.println(sep);

            }
            // 输出摘要
            sys.out.printlnf("%d comments", list.size());
        }
    }

}
