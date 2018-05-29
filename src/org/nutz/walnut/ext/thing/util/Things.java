package org.nutz.walnut.ext.thing.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.thing.WnThingService;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public abstract class Things {

    public static final int TH_LIVE = 1;
    public static final int TH_DEAD = -1;

    /**
     * @param io
     *            IO 接口
     * @param oRefer
     *            参考对象
     * @param id
     *            Thing索引的ID
     * @return Thing的索引对象
     */
    public static WnObj getThIndex(WnIo io, WnObj oRefer, String id) {
        WnObj oIndex = dirTsIndex(io, oRefer);
        WnQuery q = Wn.Q.pid(oIndex);
        q.setv("id", id);
        return io.getOne(q);
    }

    /**
     * @see #getThIndex(WnIo, WnObj, String)
     */
    public static WnObj checkThIndex(WnIo io, WnObj oRefer, String id) {
        WnObj oT = getThIndex(io, oRefer, id);
        if (null == oT)
            throw Er.create("e.cmd.thing.noexists", id + " in " + oRefer);
        return oT;
    }

    /**
     * @param sys
     *            命令系统上下文
     * @param hc
     *            命令控制器调用上下文
     * @return Thing 的索引对象
     */
    public static WnObj checkThIndex(WnSystem sys, JvmHdlContext hc) {
        String thId = hc.params.val_check(0);
        return checkThIndex(sys.io, hc.oRefer, thId);
    }

    /**
     * 找到一个对象所在的 ThingSet
     * 
     * @param o
     *            参考对象
     * @return ThingSet。 null 表示给出的对象不在一个 ThingSet 里
     */
    public static WnObj getThingSet(WnObj o) {
        // 自己就是
        if (o.isType("thing_set")) {
            return o;
        }

        // 找祖先
        while (o.hasParent()) {
            o = o.parent();
            if (o.isType("thing_set"))
                return o;
        }

        // 木找到
        return null;
    }

    /**
     * @see #getThingSet(WnObj)
     */
    public static WnObj checkThingSet(WnObj o) {
        WnObj oTS = getThingSet(o);
        if (null == oTS)
            throw Er.create("e.cmd.thing.notInThingSet", o);
        return oTS;
    }

    public static WnObj checkThingSet(WnSystem sys, String id) {
        WnObj o;
        // 用 ID
        if (Wn.isFullObjId(id)) {
            o = sys.io.checkById(id);
        }
        // 用路径
        else {
            o = Wn.checkObj(sys, id);
        }
        WnObj oTS = getThingSet(o);
        if (null == oTS)
            throw Er.create("e.cmd.thing.notInThingSet", o);
        return oTS;
    }

    // ..................................... ThingSet 的关键目录

    public static WnObj dirTsConf(WnIo io, WnObj oRefer) {
        WnObj oTS = checkThingSet(oRefer);
        return io.check(oTS, "thing.js");
    }

    public static WnObj dirTsConf(WnSystem sys, JvmHdlContext hc) {
        return dirTsConf(sys.io, hc.oRefer);
    }

    public static WnObj dirTsIndex(WnIo io, WnObj oRefer) {
        WnObj oTS = checkThingSet(oRefer);
        return io.check(oTS, "index");
    }

    public static WnObj dirTsIndex(WnSystem sys, JvmHdlContext hc) {
        return dirTsIndex(sys.io, hc.oRefer);
    }

    public static WnObj dirTsComment(WnIo io, WnObj oRefer) {
        WnObj oTS = checkThingSet(oRefer);
        return io.check(oTS, "comment");
    }

    public static WnObj dirTsComment(WnSystem sys, JvmHdlContext hc) {
        return dirTsComment(sys.io, hc.oRefer);
    }

    public static WnObj dirTsData(WnIo io, WnObj oRefer) {
        WnObj oTS = checkThingSet(oRefer);
        return io.check(oTS, "data");
    }

    public static WnObj dirTsData(WnSystem sys, JvmHdlContext hc) {
        return dirTsData(sys.io, hc.oRefer);
    }

    // ..................................... Thing 的附件目录

    public static WnObj dirThMedia(WnIo io, WnObj oTS, WnObj oT) {
        WnObj oData = dirTsData(io, oTS);
        return io.createIfNoExists(oData, oT.id() + "/media", WnRace.DIR);
    }

    public static WnObj dirThAttachment(WnIo io, WnObj oTS, WnObj oT) {
        WnObj oData = dirTsData(io, oTS);
        return io.createIfNoExists(oData, oT.id() + "/attachment", WnRace.DIR);
    }

    public static WnObj dirThResource(WnIo io, WnObj oTS, WnObj oT) {
        WnObj oData = dirTsData(io, oTS);
        return io.createIfNoExists(oData, oT.id() + "/resource", WnRace.DIR);
    }

    // /**
    // * 根据格式如 “TsID[/ThID]” 的字符串，得到 ThingSet 或者 Thing 对象
    // *
    // * @param io
    // * IO 接口
    // * @param str
    // * 描述字符串，格式为 TsID[/ThID]
    // * @return ThingSet 或者 Thing索引对象
    // */
    // public static WnObj checkRefer(WnIo io, String str) {
    // String[] ss = Strings.splitIgnoreBlank(str, "/");
    // // 指定了 TsID
    // if (ss.length == 1) {
    // return io.checkById(ss[0]);
    // }
    // // 指定了 TsID/ThId
    // WnObj oTS = io.checkById(ss[0]);
    // WnObj oTsIndexHome = checkThingSetDir(io, oTS, "index");
    // return io.check(oTsIndexHome, ss[1]);
    // }

    /**
     * 根据参数填充元数据
     * 
     * @param sys
     *            系统接口
     * 
     * @param params
     *            参数表
     * 
     * @return 填充完毕的元数据
     */
    public static NutMap fillMeta(WnSystem sys, ZParams params) {
        // 得到所有字段
        String json = Cmds.getParamOrPipe(sys, params, "fields", false);
        NutMap meta = Strings.isBlank(json) ? new NutMap() : Lang.map(json);

        // 摘要
        if (params.has("brief")) {
            meta.put("brief", params.get("brief"));
        }

        // 所有者
        if (params.has("ow")) {
            meta.put("th_ow", params.get("ow"));
        }

        // 分类
        if (params.has("cate")) {
            meta.put("th_cate", params.get("cate"));
        }

        // 内容类型
        if (params.has("tp")) {
            meta.put("tp", params.get("tp"));
        }

        // 确保类型变成内容类型
        if (meta.has("tp")) {
            String tp = meta.getString("tp");
            meta.remove("tp");
            String mime = sys.io.mimes().getMime(tp, "text/plain");
            meta.put("mime", mime);
        }

        // 将日期的字符串，搞一下
        for (Map.Entry<String, Object> en : meta.entrySet()) {
            Object v = en.getValue();
            if (null != v && v instanceof String) {
                String s = v.toString();
                Object v2 = Wn.fmt_str_macro(s);
                en.setValue(v2);
            }
        }

        // 返回传入的元数据
        return meta;
    }

    public static void doFileObj2(WnSystem sys,
                                  JvmHdlContext hc,
                                  WnObj oTs,
                                  WnObj oT,
                                  String dirName) {
        WnThingService ths = new WnThingService(sys.io, oTs);

        // 添加
        if (hc.params.has("add")) {
            String read = hc.params.get("read");
            String fnm = hc.params.get("add");
            String dupp = hc.params.get("dupp");
            boolean overwrite = hc.params.is("overwrite");
            Object src = null;
            // 从输出流中读取
            if ("true".equals(read)) {
                src = sys.in.getInputStream();
            }
            // 从文件读取
            else if (!Strings.isBlank(read)) {
                src = Wn.getObj(sys, read);
            }

            // 最后计入输出
            hc.output = ths.fileAdd(dirName, oT, fnm, src, dupp, overwrite);
        }
        // 仅仅是更新计数
        else if (hc.params.is("ufc")) {
            hc.output = ths.fileUpdateCount(dirName, oT);
        }
        // 删除
        else if (hc.params.is("del")) {
            String[] fnms;
            if (hc.params.vals.length > 1) {
                fnms = Arrays.copyOfRange(hc.params.vals, 1, hc.params.vals.length);
            } else {
                fnms = new String[0];
            }

            // 最后计入输出
            hc.output = ths.fileDelete(dirName, oT, fnms);
        }
        // 获取某指定文件
        else if (hc.params.has("get")) {
            String fnm = hc.params.get("get");
            boolean quiet = hc.params.is("quiet");

            // 最后计入输出
            hc.output = ths.fileGet(dirName, oT, fnm, quiet);
        }
        // 获取某指定文件内容
        else if (hc.params.has("cat")) {
            String fnm = hc.params.get("cat");
            String etag = hc.params.getString("etag");
            String range = hc.params.getString("range");
            String userAgent = hc.params.getString("UserAgent");
            boolean quiet = hc.params.is("quiet");
            hc.output = ths.fileRead(dirName, oT, fnm, etag, range, userAgent, quiet);
        }
        // 那么就是查询咯
        else {
            NutMap sort = Lang.map(hc.params.get("sort", "nm:1"));
            hc.output = ths.fileQuery(dirName, oT, sort);
        }
    }

    public static void update_file_count(WnIo io, WnObj oT, String countKey, WnQuery q) {
        // 首先查询一下相关的文件
        List<WnObj> oFiles = io.query(q.asc("nm"));

        // 设置: 计数
        oT.put(String.format("th_%s_nb", countKey), oFiles.size());

        // 准备统计
        List<String> fIds = new ArrayList<String>(oFiles.size());
        List<NutBean> list = new ArrayList<>(oFiles.size());
        for (WnObj oF : oFiles) {
            fIds.add(oF.id());
            list.add(oF.pick("id",
                             "nm",
                             "thumb",
                             "mime",
                             "tp",
                             "duration",
                             "width",
                             "height",
                             "video_frame_count",
                             "video_frame_rate"));
        }
        // 设置
        oT.put(String.format("th_%s_ids", countKey), fIds);
        oT.put(String.format("th_%s_list", countKey), list);

        // 持久化
        io.set(oT, "^(th_" + countKey + "_(nb|ids|map|list))$");
    }

    // ..........................................................
    // 纯帮助函数集合
    private Things() {}
}
