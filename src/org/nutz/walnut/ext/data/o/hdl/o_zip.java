package org.nutz.walnut.ext.data.o.hdl;

import java.io.OutputStream;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Stopwatch;
import org.nutz.lang.Streams;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.data.o.OContext;
import org.nutz.walnut.ext.data.o.OFilter;
import org.nutz.walnut.ext.data.o.util.WnObjMatrix;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.impl.box.cmd.cmd_zip;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wlang;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.ZParams;
import org.nutz.walnut.util.archive.WnArchiveWriting;
import org.nutz.walnut.util.archive.impl.WnZipArchiveWriting;
import org.nutz.walnut.util.bean.WnBeanMapping;
import org.nutz.walnut.util.obj.WnObjRenamingImpl;
import org.nutz.walnut.util.validate.impl.AutoMatch;

public class o_zip extends OFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqn", "^(only|quiet|hide|json)$");
    }

    @Override
    protected void process(WnSystem sys, OContext fc, ZParams params) {
        //
        // 分析参数
        //
        String as = params.getString("as", "zip");
        boolean quiet = params.is("quiet");
        boolean hide = params.is("hide");
        boolean asJson = params.is("json");
        if (asJson) {
            quiet = true;
        }
        // 过滤器
        AutoMatch am = null;
        String fltJson = params.get("m");
        if (!Ws.isBlank(fltJson)) {
            Object flt = Json.fromJson(fltJson);
            am = new AutoMatch(flt);
        }
        //
        // 分析上下文对象列表
        //
        String basePath = params.getString("base");
        WnObj oBase = null;
        if (!Ws.isBlank(basePath)) {
            oBase = Wn.checkObj(sys, basePath);
        }
        WnObjMatrix objMat = new WnObjMatrix(oBase, fc.list);
        //
        // 得到基础目录
        //
        WnObj oTop = objMat.getTopOrRoot(sys.io);

        //
        // 得到重命名规则
        //
        WnObjRenamingImpl rename = null;

        if (params.has("fnm")) {
            rename = new WnObjRenamingImpl();
            String nameTmpl = params.getString("fnm");
            rename.setNameTmpl(nameTmpl);

            // 需要映射
            WnBeanMapping mapping = null;
            String metaS = params.get("meta");
            if (null != metaS) {
                mapping = new WnBeanMapping();
                // 读取标准输入
                if ("true".equals(metaS)) {
                    String metaJson = sys.in.readAll();
                    if (!Ws.isBlank(metaJson)) {
                        NutMap meta = Wlang.map(metaJson);
                        mapping.setFields(meta, sys);
                    }
                }
                // 必然是一个路径
                else {
                    mapping.loadFrom(metaS, sys);
                }
                // 设置映射
                rename.setMapping(mapping);
                rename.setOnlyMapping(params.is("only"));
            }
        }

        // 准备压缩包
        OutputStream ops = null;
        WnArchiveWriting ag = null;
        WnObj oZip = null;

        // 准备计时
        Stopwatch sw = Stopwatch.begin();

        try {
            String phTa = params.val(0, oTop.name() + "." + as);
            // 准备输出文件
            String aphTa = Wn.normalizeFullPath(phTa, sys);
            oZip = sys.io.createIfNoExists(null, aphTa, WnRace.FILE);

            if (!quiet) {
                sys.out.printlnf("Zip %d objs to %s:", objMat.objs.size(), oZip.path());
            }

            // 准备输出流
            ops = sys.io.getOutputStream(oZip, 0);
            ag = new WnZipArchiveWriting(ops);

            // 开始逐个加入压缩包
            for (WnObj o : objMat.objs) {
                cmd_zip.addEntry(sys, oTop, ag, rename, o, quiet, am, hide);
            }
        }
        // 错误
        catch (Exception e) {
            throw Er.wrap(e);
        }
        // 确保写入
        finally {
            Streams.safeFlush(ag);
            Streams.safeClose(ag);
            Streams.safeClose(ops);
            sw.stop();
        }

        // 主程序就不要输出了
        fc.quiet = true;
        // 输出
        if (asJson) {
            JsonFormat jfmt = Cmds.gen_json_format(params);
            String json = Json.toJson(oZip, jfmt);
            sys.out.println(json);
        }
        // 非静默输出
        else if (!quiet) {
            sys.out.printlnf("Gen %s in  %s", oZip, sw.toString());
        }
    }

}
