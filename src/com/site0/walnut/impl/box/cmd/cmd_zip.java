package com.site0.walnut.impl.box.cmd;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.lang.Stopwatch;
import org.nutz.lang.Streams;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Wpath;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;
import com.site0.walnut.util.archive.WnArchiveWriting;
import com.site0.walnut.util.archive.impl.WnZipArchiveWriting;
import com.site0.walnut.util.bean.WnObjAnMatrix;
import com.site0.walnut.util.obj.WnObjRenaming;
import com.site0.walnut.util.validate.WnMatch;
import com.site0.walnut.util.validate.impl.AutoMatch;

public class cmd_zip extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        // 分析参数
        ZParams params = ZParams.parse(args, "^(quiet|hide)");
        String phTa = params.val_check(0);
        boolean quiet = params.is("quiet");
        boolean hide = params.is("hide");

        if (params.vals.length <= 1) {
            throw Er.create("e.cmd.zip.NilInput");
        }

        // 准备计时
        Stopwatch sw = Stopwatch.begin();

        // 过滤器
        AutoMatch am = null;
        String fltJson = params.get("m");
        if (!Ws.isBlank(fltJson)) {
            Object flt = Json.fromJson(fltJson);
            am = new AutoMatch(flt);
        }

        // 准备输入源
        List<WnObj> oSrcList = new ArrayList<>(params.vals.length - 1);
        for (int i = 1; i < params.vals.length; i++) {
            String rph = params.val(i);
            WnObj o = Wn.checkObj(sys, rph);
            oSrcList.add(o);
        }

        // 得到一个公共的父对象
        WnObj oTop;
        if (oSrcList.size() == 1) {
            oTop = oSrcList.get(0);
        } else {
            oTop = this.findCommonParent(oSrcList);
        }

        OutputStream ops = null;
        WnArchiveWriting ag = null;
        WnObj oZip = null;

        try {
            // 准备输出文件
            String aphTa = Wn.normalizeFullPath(phTa, sys);
            oZip = sys.io.createIfNoExists(null, aphTa, WnRace.FILE);

            // 准备实体映射以便防重
            Map<String, Boolean> rphMemo = new HashMap<>();

            // 准备输出流
            ops = sys.io.getOutputStream(oZip, 0);
            ag = new WnZipArchiveWriting(ops);

            // 开始逐个加入压缩包
            int count = 0;
            for (WnObj o : oSrcList) {
                count = addEntry(sys, oTop, ag, null, o, quiet, am, hide, rphMemo, count);
            }
        }
        // 确保写入
        finally {
            Streams.safeFlush(ag);
            Streams.safeClose(ag);
            Streams.safeClose(ops);
            sw.stop();
        }

        if (!quiet) {
            sys.out.printlnf("Gen %s in  %s", oZip, sw.toString());
        }
    }

    public static int addEntry(WnSystem sys,
                               WnObj oTop,
                               WnArchiveWriting ag,
                               WnObjRenaming rename,
                               WnObj o,
                               boolean quiet,
                               WnMatch am,
                               boolean hide,
                               Map<String, Boolean> memo,
                               int count)
            throws IOException {
        // 无视隐藏文件
        if (!hide && o.isHidden()) {
            return count;
        }
        // 目录，递归
        if (o.isDIR()) {
            List<WnObj> children = sys.io.getChildren(o, null);
            for (WnObj child : children) {
                count += addEntry(sys, oTop, ag, rename, child, quiet, am, hide, memo, count);
            }
        }
        // 无视不符合条件的文件
        else if (null != am && !am.match(o)) {
            return count;
        }
        // 文件，写入
        else {
            String rph = Wn.Io.getRelativePath(oTop, o);
            InputStream ins = sys.io.getInputStream(o, 0);
            String newName = null;
            String enPath = rph;
            long len = o.len();
            if (null != rename) {
                newName = rename.getName(o);
                // 去掉不合法的字符串
                newName = newName.replaceAll("['\"\\/]", "_");
                // 路径改名
                if (!Ws.isBlank(newName)) {
                    enPath = Wpath.renamePath(rph, newName);
                }
            }
            if (!quiet) {
                if (null != newName) {
                    sys.out.printlnf(" %d). %s >> %s : %s", count, rph, newName, o.toString());
                } else {
                    sys.out.printlnf(" %d) %s : %s", count, rph, o.toString());
                }
            }
            if (memo.containsKey(enPath)) {
                if (!quiet) {
                    sys.out.printlnf(" !!!DUP!!! %d). %s >> %s : %s",
                                     count,
                                     rph,
                                     newName,
                                     o.toString());
                }
            } else {
                ag.addFileEntry(enPath, ins, len);
                memo.put(enPath, true);
            }
            count++;
        }
        return count;
    }

    private WnObj findCommonParent(List<WnObj> objs) {
        if (null == objs)
            return null;
        if (objs.size() == 1) {
            return objs.get(0);
        }
        WnObjAnMatrix oam = new WnObjAnMatrix(objs);
        return oam.findCommonParent(null);
    }

}
