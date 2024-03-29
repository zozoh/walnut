package com.site0.walnut.ext.data.o.hdl;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Stopwatch;
import org.nutz.lang.Streams;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.ext.data.o.OContext;
import com.site0.walnut.ext.data.o.OFilter;
import com.site0.walnut.ext.data.o.util.WnObjMatrix;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.impl.box.cmd.cmd_zip;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;
import com.site0.walnut.util.archive.WnArchiveWriting;
import com.site0.walnut.util.archive.impl.WnZipArchiveWriting;
import com.site0.walnut.util.bean.WnBeanMapping;
import com.site0.walnut.util.obj.WnObjRenamingImpl;
import com.site0.walnut.util.validate.impl.AutoMatch;

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
            String sMapping = params.get("mapping");
            if (null != sMapping) {
                mapping = new WnBeanMapping();
                // 读取标准输入
                if ("true".equals(sMapping)) {
                    String metaJson = sys.in.readAll();
                    if (!Ws.isBlank(metaJson)) {
                        NutMap meta = Wlang.map(metaJson);
                        mapping.setFields(meta, sys);
                    }
                }
                // 必然是一个路径
                else {
                    mapping.loadFrom(sMapping, sys);
                }
                // 设置映射
                rename.setMapping(mapping);
                rename.setOnlyMapping(params.is("only"));
            }
        }

        // 准备输出文件元数据
        NutMap meta = null;
        if (params.has("meta")) {
            String sMeta = params.getString("meta");
            meta = Wlang.map(sMeta);
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

            // 准备实体映射以便防重
            Map<String, Boolean> rphMemo = new HashMap<>();

            // 开始逐个加入压缩包
            int count = 0;
            for (WnObj o : objMat.objs) {
                count = cmd_zip.addEntry(sys, oTop, ag, rename, o, quiet, am, hide, rphMemo, count);
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

        // 更新元数据
        if (null != oZip && null != meta && !meta.isEmpty()) {
            Wn.explainMetaMacro(meta);
            sys.io.appendMeta(oZip, meta);
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
