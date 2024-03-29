package com.site0.walnut.ext.data.thing.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Files;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.Strings;
import com.site0.walnut.util.tmpl.WnTmpl;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.WnExecutable;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.ext.data.thing.WnThingService;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;

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
        // 获取 ID
        WnObj oT = io.getIn(oIndex, id);

        // 默认为其设置上父集合 ID
        if (null != oT) {
            // 确保不超出数据集
            if (!oT.isMyParent(oIndex)) {
                return null;
            }

            // 为了确保在数据集场景下出来的数据默认 th_live=1
            // 在数据表里不记录这个的前提下，默认放一个进去
            oT.putDefault("th_live", 1);

            // 确保有数据集的名称
            oT.put("th_set", oIndex.parentId());
        }
        return oT;
        // switch (oRefer.getString("thing-by", "wntree")) {
        // case "sql": {
        // SqlThingContext ctx = SqlThingMaster.me().getSqlThingContext(oRefer);
        // Record re = ctx.dao.fetch(ctx.table, Cnd.where("id", "=", id));
        // if (re == null)
        // return null;
        // return SqlThingMaster.asWnObj(oRefer, oIndex, re.sensitive());
        // }
        // default:
        // case "wntree": {
        // WnQuery q = Wn.Q.pid(oIndex);
        // q.setv("id", id);
        // WnObj oT = io.get(id);
        // if (null != oT) {
        // oT.put("th_set", oIndex.parentId());
        // }
        // return oT;
        // }
        // }
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
     * @param o
     *            对象
     * @return 给定对象是否是一个 ThingSet 的主目录
     */
    public static boolean isThingSet(WnObj o) {
        if (null == o)
            return false;
        return o.isType("thing_set");
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
        if (isThingSet(o)) {
            return o;
        }

        // 找祖先
        while (o.hasParent()) {
            o = o.parent();
            if (isThingSet(o))
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

    public static WnObj fileTsConf(WnIo io, WnObj oRefer) {
        WnObj oTS = checkThingSet(oRefer);
        return io.check(oTS, "thing.json");
    }

    public static WnObj fileTsConf(WnSystem sys, JvmHdlContext hc) {
        return fileTsConf(sys.io, hc.oRefer);
    }

    public static WnObj dirTsIndex(WnIo io, WnObj oRefer) {
        WnObj oTS = checkThingSet(oRefer);
        WnObj oD = io.check(oTS, "index");
        return Wn.real(oD, io, new HashMap<>());
    }

    public static WnObj dirTsIndex(WnSystem sys, JvmHdlContext hc) {
        return dirTsIndex(sys.io, hc.oRefer);
    }

    public static WnObj dirTsComment(WnIo io, WnObj oRefer) {
        WnObj oTS = checkThingSet(oRefer);
        WnObj oD = io.check(oTS, "comment");
        return Wn.real(oD, io, new HashMap<>());
    }

    public static WnObj dirTsComment(WnSystem sys, JvmHdlContext hc) {
        return dirTsComment(sys.io, hc.oRefer);
    }

    public static WnObj dirTsTmpFile(WnIo io, WnObj oRefer) {
        WnObj oTS = checkThingSet(oRefer);
        return io.createIfNoExists(oTS, "tmp", WnRace.DIR);
    }

    public static WnObj dirTsTmpFile(WnSystem sys, JvmHdlContext hc) {
        return dirTsComment(sys.io, hc.oRefer);
    }

    public static WnObj dirTsData(WnIo io, WnObj oRefer) {
        WnObj oTS = checkThingSet(oRefer);
        WnObj oD = io.check(oTS, "data");
        return Wn.real(oD, io, new HashMap<>());
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

    public static WnObj createFileNoDup(WnIo io, WnObj oDir, String fnm, String dupp) {
        // 准备默认的模板
        if ("true".equals(dupp)) {
            dupp = "@{major}(@{nb})@{suffix}";
        }
        // 准备文件名模板
        NutMap c = new NutMap();
        c.put("major", Files.getMajorName(fnm));
        c.put("suffix", Files.getSuffix(fnm));
        WnTmpl seg = Cmds.parse_tmpl(dupp);
        // 挨个尝试新的文件名
        int i = 1;
        do {
            c.put("nb", i++);
            fnm = seg.render(c);
        } while (io.exists(oDir, fnm));
        // 创建
        return io.create(oDir, fnm, WnRace.FILE);
    }

    public static NutMap formatMeta(NutMap meta) {
        // 将日期的字符串，搞一下
        Wn.explainMetaMacro(meta);
        // 返回元数据
        return meta;
    }

    public static void doFileObj2(WnSystem sys,
                                  JvmHdlContext hc,
                                  WnObj oTs,
                                  WnObj oT,
                                  String dirName) {
        WnThingService ths = new WnThingService(sys, oTs);

        // 添加
        if (hc.params.has("add") || hc.params.has("upload")) {
            // 分析参数
            String read = hc.params.getString("read");
            String fnm = hc.params.getString("add");
            String dupp = hc.params.getString("dupp");
            boolean overwrite = hc.params.is("overwrite");
            String ukey = hc.params.getString("ukey");

            // 分析一下是上传还是添加
            String upload = hc.params.getString("upload");
            if (Strings.isBlank(upload) || upload.indexOf("boundary=") < 0) {
                upload = null;
            }

            // 准备一个代表性的文件对象，以便 处理 ukey
            WnObj oMedia = null;

            // 文件上传流
            if (null != upload) {
                InputStream ins = null;
                // 从输出流中读取
                if ("true".equals(read)) {
                    ins = sys.in.getInputStream();
                }
                // 从文件读取
                else if (!Strings.isBlank(read)) {
                    WnObj o = Wn.getObj(sys, read);
                    ins = sys.io.getInputStream(o, 0);
                }
                String fieldName = hc.params.getString("upfield", null);
                List<WnObj> oList = ths.fileUpload(dirName,
                                                   oT,
                                                   fnm,
                                                   ins,
                                                   upload,
                                                   fieldName,
                                                   dupp,
                                                   overwrite);
                if (!oList.isEmpty()) {
                    oMedia = oList.get(0);
                }
                hc.output = oList;
            }
            // 普通文件输入流
            else {
                // 准备输入
                Object src = null;
                // 从输出流中读取
                if ("true".equals(read) || Ws.isBlank(read)) {
                    src = sys.in.getInputStream();
                }
                // 从文件读取
                else {
                    src = Wn.getObj(sys, read);
                }

                // 最后计入输出
                oMedia = ths.fileAdd(dirName, oT, fnm, src, dupp, overwrite);
                hc.output = oMedia;
            }

            // 如果指定了更新键
            if (!Strings.isBlank(ukey) && null != oMedia) {
                sys.io.appendMeta(oT, Wlang.map(ukey, "id:" + oMedia.id()));
            }
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
        // 获取某指定文件内容（HTTP 流输出）
        else if (hc.params.has("http")) {
            String fnm = hc.params.get("http");
            String etag = hc.params.getString("etag");
            String range = hc.params.getString("range");
            String userAgent = null;
            boolean quiet = hc.params.is("quiet");
            // 下载的话，要设置 UA，这样根据 UA 可以得到 HTTP响应头的那个下载目标的设置
            // Safari 与 WebKit 等有点不一样
            if (hc.params.is("download")) {
                userAgent = hc.params.getString("UserAgent");
            }
            hc.output = ths.fileReadAsHttp(dirName, oT, fnm, etag, range, userAgent, quiet);
        }
        // 直接输出指定文件内容
        else if (hc.params.has("cat")) {
            String fnm = hc.params.get("cat");
            boolean quiet = hc.params.is("quiet");

            WnObj o = ths.fileGet(dirName, oT, fnm, quiet);
            if (null != o) {
                hc.output = sys.io.getInputStream(o, 0);
            }
        }
        // 那么就是查询咯
        else {
            NutMap sort = Wlang.map(hc.params.get("sort", "nm:1"));
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
                             "sha1",
                             "tp",
                             "len",
                             "duration",
                             "width",
                             "height",
                             "video_frame_count",
                             "video_frame_rate",
                             "lm",
                             "ct"));
        }
        // 设置
        oT.put(String.format("th_%s_ids", countKey), fIds);
        oT.put(String.format("th_%s_list", countKey), list);

        // 持久化
        io.set(oT, "^(th_" + countKey + "_(nb|ids|map|list))$");
    }

    public static void runCommand(NutBean context,
                                  String cmd,
                                  WnExecutable executor,
                                  StringBuilder stdOut,
                                  StringBuilder stdErr) {
        // Guard
        if (null == context || null == executor || Strings.isBlank(cmd)) {
            return;
        }
        

        // 分析命令模板
        String cmdText = Strings.trim(WnTmpl.exec(cmd, context));
        String input = null;

        // 要读取输入的
        if (cmdText.startsWith("|")) {
            cmdText = cmdText.substring(1);
            JsonFormat jfmt = JsonFormat.compact().setQuoteName(true).setIgnoreNull(false);
            input = Json.toJson(context, jfmt);
        }

        // 执行
        executor.exec(cmdText, stdOut, stdErr, input);
    }

    public static String runCommand(NutBean context, String cmd, WnExecutable executor) {
        if (!Strings.isBlank(cmd)) {
            return runCommands(context, Wlang.array(cmd), executor);
        }
        return null;
    }

    public static String runCommands(NutBean context, String[] cmds, WnExecutable executor) {
        if (null == cmds || cmds.length == 0 || null == executor) {
            return null;
        }

        StringBuilder stdOut = new StringBuilder();
        StringBuilder stdErr = new StringBuilder();

        for (String cmd : cmds) {
            runCommand(context, cmd, executor, stdOut, stdErr);

            // 出错就阻止后续执行
            if (stdErr.length() > 0)
                throw Er.wrap(stdErr.toString());
        }

        return stdOut.toString();
    }

    // ..........................................................
    // 纯帮助函数集合
    private Things() {}
}
