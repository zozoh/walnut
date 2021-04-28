package org.nutz.walnut.ext.data.thing.hdl;

import java.util.LinkedList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.Times;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.data.thing.util.Things;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnPager;

@JvmHdlParamArgs(value = "cnqihbslVNHQ", regex = "^(pager|quick|agree)$")
public class thing_comment implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 得到对应对 Thing
        WnObj oT = Things.checkThIndex(sys, hc);
        
        // 得到对应的 comment 目录
        // TODO 这个命令实现有问题，需要修改一下，因为 comment 变成单文件了

        // # 添加注释，会自动修改 task.th_c_cmt 字段
        // thing xxx comment -add "搞定了，呼"
        if (hc.params.has("add")) {
            // 得到对应的注释数据目录
            WnObj oTCHome = sys.io.createIfNoExists(oT, "comments", WnRace.DIR);
            // 执行
            this._do_add(sys, hc, oT, oTCHome);
        }
        // # 删除注释，会自动修改 task.th_c_cmt 字段
        // thing xxx comment -del 20150721132134321
        else if (hc.params.has("del")) {
            // 得到对应的注释数据目录
            WnObj oTCHome = sys.io.fetch(oT, "comments");
            // 执行
            if (null != oTCHome)
                this._do_del(sys, hc, oT, oTCHome);
            else
                throw Er.create("e.cmd.thing.comment.noDataHome", oT.id());
        }
        // # 修改注释
        // thing xxx comment 20150721132134321 "修改一下注释"
        else if (hc.params.vals.length > 0) {
            // 得到对应的注释数据目录
            WnObj oTCHome = sys.io.fetch(oT, "comments");
            // 执行
            if (null != oTCHome)
                this._do_set(sys, hc, oT, oTCHome);
            else
                throw Er.create("e.cmd.thing.comment.noDataHome", oT.id());
        }
        // # 获取某个注释全部属性(自动确保读取 content)
        // thing xxx comment -get 20150721132134321
        else if (hc.params.has("get")) {
            // 得到对应的注释数据目录
            WnObj oTCHome = sys.io.fetch(oT, "comments");
            // 执行
            if (null != oTCHome)
                this._do_get(sys, hc, oT, oTCHome);
        }
        // # 获取某个注释的内容文本（仅仅是内容文本，不是 breif)
        // thing xxx comment -read 20150721132134321
        else if (hc.params.has("read")) {
            // 得到对应的注释数据目录
            WnObj oTCHome = sys.io.fetch(oT, "comments");
            // 执行
            if (null != oTCHome)
                // 执行
                this._do_read(sys, hc, oT, oTCHome);
        }
        // # 查询所有的注释
        // thing xxx comment
        // - 支持 'sort|pager|limit|skip|json|out|t' 等参数
        else {
            // 得到对应的注释数据目录
            WnObj oTCHome = sys.io.fetch(oT, "comments");
            // 执行
            this._do_query(sys, hc, oT, oTCHome);
        }
    }

    private void _do_add(WnSystem sys, JvmHdlContext hc, WnObj oT, WnObj oTCHome) {
        // 得到要修改的内容
        String content = Cmds.getParamOrPipe(sys, hc.params, "add", false);

        // 检查内容
        __verify_comment_content(hc, oT, content);

        // 创建评论对象
        String cnm = Times.format("yyyyMMddHHmmddSSS", Times.now());
        WnObj oC = sys.io.createIfNoExists(oTCHome, cnm, WnRace.FILE);

        // 修改一下注释的必要元数据
        oC.setv("th_set", oT.getString("th_set"));
        oC.setv("th_id", oT.id());
        sys.io.set(oC, "^th_(set|id)$");

        // 写入内容
        __write_comment_content(sys, hc, oC, content);
    }

    private void _do_del(WnSystem sys, JvmHdlContext hc, WnObj oT, WnObj oTCHome) {
        String cnm = hc.params.check("del");
        WnObj oC = __check_comment_obj(sys, oTCHome, cnm);

        sys.io.delete(oC);
        sys.io.inc(oC.id(), "th_c_cmt", -1, false);
    }

    private void _do_set(WnSystem sys, JvmHdlContext hc, WnObj oT, WnObj oTCHome) {
        // 得到要修改的内容
        String content = Cmds.getParamOrPipe(sys, hc.params, 1);

        // 检查内容
        __verify_comment_content(hc, oT, content);

        // 取得评论对象
        String cnm = hc.params.val_check(0);
        WnObj oC = __check_comment_obj(sys, oTCHome, cnm);

        // 写入内容
        __write_comment_content(sys, hc, oC, content);
    }

    private void _do_get(WnSystem sys, JvmHdlContext hc, WnObj oT, WnObj oTCHome) {
        String cnm = hc.params.check("get");
        WnObj oC = __check_comment_obj(sys, oTCHome, cnm);
        hc.output = this.__check_comment_content(sys, oC);
    }

    private void _do_read(WnSystem sys, JvmHdlContext hc, WnObj oT, WnObj oTCHome) {
        String cnm = hc.params.check("read");
        WnObj oC = __check_comment_obj(sys, oTCHome, cnm);

        this.__check_comment_content(sys, oC);

        hc.output = oC.getString("content");
    }

    private void _do_query(WnSystem sys, JvmHdlContext hc, WnObj oT, WnObj oTCHome) {
        // ..............................................
        // 准备分页信息
        WnPager wp = new WnPager(hc.params);

        // 没有数据
        if (null == oTCHome) {
            hc.pager = wp;
            hc.output = new LinkedList<WnObj>();
            return;
        }

        // ..............................................
        // 准备查询条件
        String qStr = hc.params.val(0);
        WnQuery q = new WnQuery();
        // 指定了条件
        if (!Strings.isBlank(qStr)) {
            // 条件是"或"
            if (Strings.isQuoteBy(qStr, '[', ']')) {
                List<NutMap> ors = Json.fromJsonAsList(NutMap.class, qStr);
                q.addAll(ors);
            }
            // 条件是"与"
            else {
                q.add(Lang.map(qStr));
            }
        }
        // 未指定条件
        else {
            q.first();
        }
        // 确保限定了集合
        NutMap map = new NutMap();
        map.put("th_id", oT.id());
        map.put("th_set", oT.getString("th_set"));
        map.put("pid", oTCHome.id());
        q.setAllToList(map);

        // ..............................................
        // 设置分页信息
        if (null != wp) {
            wp.setupQuery(sys, q);
        }

        // 设置排序
        if (hc.params.has("sort")) {
            NutMap sort = Lang.map(hc.params.check("sort"));
            q.sort(sort);
        }

        // ..............................................
        // 执行查询
        List<WnObj> list = sys.io.query(q);

        // 针对结果进一步处理
        if (!hc.params.is("quick"))
            for (WnObj oC : list) {
                this.__check_comment_content(sys, oC);
            }

        // ..............................................
        // 返回结果
        hc.pager = wp;
        hc.output = list;
    }

    private void __verify_comment_content(JvmHdlContext hc, WnObj oT, String content) {
        if (Strings.isBlank(content) || content.length() < hc.params.getInt("minsz", 5)) {
            throw Er.create("e.cmd.thing.comment.content.toShort", oT.id());
        }
    }

    private WnObj __check_comment_obj(WnSystem sys, WnObj oTCHome, String commentName) {
        return sys.io.check(oTCHome, commentName);
    }

    private WnObj __check_comment_content(WnSystem sys, WnObj oC) {
        if (oC.has("brief") && !oC.has("content")) {
            oC.setv("content", sys.io.readText(oC));
        }
        return oC;
    }

    private void __write_comment_content(WnSystem sys, JvmHdlContext hc, WnObj oC, String content) {
        // 如果内容太长
        int maxsz = hc.params.getInt("maxsz", 256);
        if (content.length() > maxsz) {
            String brief = content.substring(0, maxsz);

            oC.setv("brief", brief);
            oC.setv("content", null);
            Wn.set_type(sys.io.mimes(), oC, hc.params.get("tp", "txt"));

            sys.io.writeText(oC, content);
            sys.io.set(oC, "^(brief|content|tp|mime)$");
        }
        // 否则就是内容很短
        else {
            oC.setv("brief", null);
            oC.setv("content", content);
            Wn.set_type(sys.io.mimes(), oC, hc.params.get("tp", "txt"));

            sys.io.writeText(oC, "");
            sys.io.set(oC, "^(brief|content|tp|mime)$");
        }
    }

}
