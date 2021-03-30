package org.nutz.walnut.ext.dsync.hdl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.nutz.lang.Stopwatch;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.dsync.DSyncContext;
import org.nutz.walnut.ext.dsync.DSyncFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wlang;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.Wpath;
import org.nutz.walnut.util.ZParams;
import org.nutz.walnut.util.archive.WnArchiveReading;
import org.nutz.walnut.util.archive.WnArchiveSummary;
import org.nutz.walnut.util.archive.impl.WnZipArchiveReading;

public class dsync_unpack extends DSyncFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "fql", "^(force|quiet|list|load)$");
    }

    @Override
    protected void process(WnSystem sys, DSyncContext fc, ZParams params) {
        // 分析参数: 必须指定一个包指纹
        String sha1 = params.val_check(0);
        int buf_size = params.getInt("buf", 8192);
        boolean quiet = params.is("quiet", "q");
        boolean force = params.is("force", "f");
        boolean load = params.is("load");
        boolean justList = params.is("list", "l");

        // 得到包名
        String pkgName = fc.api.getPackageName(fc.config, sha1);
        String majName = fc.config.getName() + "/" + sha1;

        // 得到包文件
        fc.oArchive = Wn.checkObj(sys, "~/.dsync/pkg/" + pkgName);

        // 准备输出目录
        String aph = Wn.normalizeFullPath("~/.dsync/cache/" + majName, sys);
        WnObj oCacheHome = sys.io.createIfNoExists(null, aph, WnRace.DIR);

        aph = Wn.normalizeFullPath("~/.dsync/data/", sys);
        WnObj oDataHome = sys.io.createIfNoExists(null, aph, WnRace.DIR);

        // 准备计时
        Stopwatch sw = Stopwatch.begin();

        // 准备包的输入流
        InputStream ins = sys.io.getInputStream(fc.oArchive, 0);

        // 准备读取方式
        WnArchiveSummary sum = new WnArchiveSummary();
        WnArchiveReading ing = new WnZipArchiveReading(ins);

        // 准备字节缓冲数组
        byte[] buf = new byte[buf_size];

        // 准备回调处理器
        ing.onNext((en, zin) -> {
            // 判断一下 MACOS特殊文件夹
            if (en.name.startsWith("__MACOSX") || en.name.contains("/__MACOSX")) {
                return;
            }

            // 输出调试信息
            if (!quiet || justList) {
                sys.out.printf(" %s: %s : ", en.dir ? "D" : "F", en.name);
            }

            // 索引树，或者元数据，写到缓冲目录里
            boolean isTree = en.name.endsWith(".tree");
            if (isTree || en.name.startsWith("meta/")) {
                WnObj o = sys.io.fetch(oCacheHome, en.name);
                if (null != o && !force) {
                    if (!quiet) {
                        sys.out.printlnf("skipped %dbytes", o.len());
                    }
                    return;
                }
                if (null == o) {
                    o = sys.io.createIfNoExists(oCacheHome, en.name, WnRace.FILE);
                    if (isTree && !o.isMime("text/plain")) {
                        sys.io.appendMeta(o, Wlang.map("mime", "text/plain"));
                    }
                }
                OutputStream ops = sys.io.getOutputStream(o, 0);
                en.writeAndClose(zin, ops, buf);
                sum.size += o.len();
                if (!quiet) {
                    sys.out.printlnf("writed %dbytes", o.len());
                }
            }
            // 数据，写到数据目录
            else if (en.name.startsWith("data/")) {
                String fnm = Wpath.getName(en.name);
                WnObj o = sys.io.fetch(oDataHome, fnm);
                if (null != o && !force) {
                    if (!quiet) {
                        sys.out.printlnf("skipped %dbytes", o.len());
                    }
                    return;
                }
                if (null == o) {
                    o = sys.io.createIfNoExists(oDataHome, fnm, WnRace.FILE);
                }
                OutputStream ops = sys.io.getOutputStream(o, 0);
                en.writeAndClose(zin, ops, buf);
                sum.size += o.len();
                if (!quiet) {
                    sys.out.printlnf("writed %dbytes", o.len());
                }
            }
        });

        // 执行读取
        try {
            sum.items = ing.readAll();

            // 重新加载上下文
            if (load) {
                fc.trees = fc.api.loadTrees(fc.config, sha1);
            }

            // 结束计时
            sw.stop();
        }
        catch (IOException e) {
            throw Er.wrap(e);
        }

        // 打印结果
        if (!quiet) {
            sys.out.printlnf("Done for unpack %s in %s", fc.oArchive, sw.toString());
            sys.out.println(sum.toString());
        }
    }

}
