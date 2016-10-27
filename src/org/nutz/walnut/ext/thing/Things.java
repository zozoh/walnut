package org.nutz.walnut.ext.thing;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
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
        return io.fetch(oIndex, id);
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

    public static WnObj checkThingSet(WnIo io, String id) {
        WnObj o = io.checkById(id);
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

        // 返回传入的元数据
        return meta;
    }

    // ..........................................................
    // 纯帮助函数集合
    private Things() {}

    /**
     * @param sys
     *            系统
     * @param hc
     *            命令控制器调用上下文
     * @param oDir
     *            文件的所在目录
     * @param oT
     *            Thing 的索引对象
     * @param key
     *            计数的键值
     */
    public static void doFileObj(WnSystem sys, JvmHdlContext hc, WnObj oDir, WnObj oT, String key) {
        // 判断是否静默输出
        boolean isQ = hc.params.is("quiet");

        // 如何写入内容
        String read = hc.params.get("read");
        WnObj oSrc = null;
        if (null != read && !"true".equals(read)) {
            oSrc = Wn.getObj(sys, read);
        }

        // 准备查询条件
        WnQuery q = Wn.Q.pid(oDir);
        q.setv("race", WnRace.FILE);

        // 添加
        if (hc.params.has("add")) {
            String fnm = hc.params.get("add");
            // 首先判断文件是否存在
            WnObj oM = sys.io.fetch(oDir, fnm);

            // 不存在，创建一个
            if (null == oM) {
                oM = sys.io.create(oDir, fnm, WnRace.FILE);
            }
            // 如果存在 ...
            else {
                // 如果存在，并且还要 -read 自己，那么就直接过
                if (null != oSrc && oSrc.isSameId(oM)) {
                    hc.output = oM;
                    return;
                }

                // 是否生成一个新的
                String dupp = hc.params.get("dupp");
                if (!Strings.isBlank(dupp)) {
                    // 准备默认的模板
                    if ("true".equals(dupp)) {
                        dupp = "@{major}(@{nb})@{suffix}";
                    }
                    // 准备文件名模板
                    NutMap c = new NutMap();
                    c.put("major", Files.getMajorName(fnm));
                    c.put("suffix", Files.getSuffix(fnm));
                    Tmpl seg = Cmds.parse_tmpl(dupp);
                    // 挨个尝试新的文件名
                    int i = 1;
                    do {
                        c.put("nb", i++);
                        fnm = seg.render(c);
                    } while (sys.io.exists(oDir, fnm));
                    // 创建
                    oM = sys.io.create(oDir, fnm, WnRace.FILE);
                }
                // 不能生成一个新的，并且还不能覆盖就抛错
                else if (!hc.params.is("overwrite")) {
                    throw Er.create("e.cmd.thing." + key + ".exists", oDir.path() + "/" + fnm);
                }
            }

            // 嗯得到一个空文件了，那么看看怎么写入内容呢？
            if (null != read) {
                // 从输出流中读取
                if ("true".equals(read)) {
                    InputStream ins = sys.in.getInputStream();
                    sys.io.writeAndClose(oM, ins);
                }
                // 否则试图从指定的文件里读取
                else if (null != oSrc) {
                    sys.io.copyData(oSrc, oM);
                    // 因为 copyData 是快速命令，所以要重新执行一下钩子
                    oM = Wn.WC().doHook("write", oM);
                }
                // 那么源文件必然不存在
                else {
                    throw Er.create("e.cmd.thing." + key + ".readNone", read);
                }
            }

            // 更新计数
            __update_file_count(sys, oT, key, q);

            // 最后计入输出
            hc.output = oM;
        }
        // 删除
        else if (hc.params.has("del")) {
            String fnm = hc.params.get("del");
            WnObj oM = sys.io.fetch(oDir, fnm);
            if (null == oM && !isQ) {
                throw Er.create("e.cmd.thing." + key + ".noexists", oDir.path() + "/" + fnm);
            }
            sys.io.delete(oM);

            // 更新计数
            __update_file_count(sys, oT, key, q);

            // 最后计入输出
            hc.output = oM;
        }
        // 那么就是查询咯
        else {
            if (hc.params.has("sort")) {
                q.sort(Lang.map(hc.params.check("sort")));
            }
            // 默认按名称排序
            else {
                q.asc("nm");
            }

            // 得到查询记过
            List<WnObj> list = sys.io.query(q);

            // 如果声明了键，看看是否需要更新计数
            if (!Strings.isBlank(key) && oT.getInt(key) != list.size()) {
                oT.setv(key, sys.io.count(q));
                sys.io.set(oT, "^" + key + "$");
            }

            // 记录到输出中
            hc.output = list;
        }
    }

    private static void __update_file_count(WnSystem sys, WnObj oT, String countKey, WnQuery q) {
        List<WnObj> oFiles = sys.io.query(q);
        oT.setv(String.format("th_%s_nb", countKey), oFiles.size());
        List<String> fIds = new ArrayList<String>(oFiles.size());
        for (WnObj oF : oFiles)
            fIds.add(oF.id());
        oT.setv(String.format("th_%s_ids", countKey), fIds);
        sys.io.set(oT, "^(th_" + countKey + "_(nb|ids))$");
    }

}
