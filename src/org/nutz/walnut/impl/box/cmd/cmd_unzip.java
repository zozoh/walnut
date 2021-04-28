package org.nutz.walnut.impl.box.cmd;

import java.io.InputStream;
import java.io.OutputStream;

import org.nutz.lang.Files;
import org.nutz.lang.Stopwatch;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.ZParams;
import org.nutz.walnut.util.archive.WnArchiveReading;
import org.nutz.walnut.util.archive.WnArchiveSummary;
import org.nutz.walnut.util.archive.impl.WnZipArchiveReading;
import org.nutz.walnut.util.validate.WnMatch;
import org.nutz.walnut.util.validate.impl.AutoStrMatch;

public class cmd_unzip extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        // 分析参数
        ZParams params = ZParams.parse(args, "flh", "^(quiet|hidden|macosx|read)");
        boolean quiet = params.is("quiet");
        boolean force = params.is("f");
        boolean hidden = params.is("hidden", "h");
        boolean macosx = params.is("macosx");
        boolean justList = params.is("l");
        boolean read = params.is("read");
        String phSrc = params.val_check(0);
        WnObj oSrc = Wn.checkObj(sys, phSrc);
        int buf_size = params.getInt("buf", 8192);

        // 过滤器
        WnMatch am = null;
        String m_str = params.get("m");
        if (!Ws.isBlank(m_str)) {
            am = new AutoStrMatch(m_str);
        }

        // 准备计时
        Stopwatch sw = Stopwatch.begin();

        // 得到输出目录，默认为当前目录
        WnObj[] oTas = new WnObj[1];
        String phTa = params.val(1, Files.getMajorName(oSrc.name()));
        String aphTa = Wn.normalizeFullPath(phTa, sys);
        if (!read) {
            oTas[0] = sys.io.createIfNoExists(null, aphTa, WnRace.DIR);
        }

        // 准备输入流
        InputStream ins = sys.io.getInputStream(oSrc, 0);

        // 准备读取方式
        WnArchiveSummary sum = new WnArchiveSummary();
        WnArchiveReading ing = new WnZipArchiveReading(ins);

        // 准备字节缓冲数组（read 模式直接写出到输出流，因此不需要缓冲）
        byte[] buf = read ? null : new byte[buf_size];

        // 准备回调处理器
        WnMatch AM = am;
        ing.onNext((en, zin) -> {
            // 判断一下隐藏文件
            if (!hidden && en.name.startsWith(".") || en.name.contains("/.")) {
                return;
            }

            // 判断一下 MACOS特殊文件夹
            if (!macosx && en.name.startsWith("__MACOSX") || en.name.contains("/__MACOSX")) {
                return;
            }

            // 名称过滤器
            if (null != AM && !AM.match(en.name)) {
                return;
            }

            // 输出调试信息
            if (!quiet || justList) {
                sys.out.printlnf(" %s: %s : %dbytes", en.dir ? "D" : "F", en.name, en.len);
            }

            // 仅仅是输出调试信息
            if (justList) {
                return;
            }

            // 如果是读取模式，那么就不创建文件了
            if (read) {
                if (en.dir) {
                    sum.dir++;
                } else {
                    sum.file++;
                    sys.out.write(zin);
                }
                return;
            }

            // 恢复目标目录
            WnObj oTa = oTas[0];

            // 是否强制写入
            WnObj o = sys.io.fetch(oTa, en.name);
            if (null != o && !force) {
                return;
            }

            // 写入对象: 目录
            if (en.dir) {
                if (null == o) {
                    sys.io.createIfNoExists(oTa, en.name, WnRace.DIR);
                }
                sum.dir++;
            }
            // 写入对象: 文件
            else {
                if (null == o) {
                    o = sys.io.createIfNoExists(oTa, en.name, WnRace.FILE);
                }
                sum.file++;
                OutputStream ops = sys.io.getOutputStream(o, 0);
                en.writeAndClose(zin, ops, buf);
                sum.size += o.len();
            }
        });

        // 执行读取
        sum.items = ing.readAll();

        // 结束计时
        sw.stop();

        // 最后输出
        if (!quiet && !read) {
            sys.out.printlnf("Done for unzip %s in %s", oSrc, sw.toString());
            sys.out.println(sum.toString());
        }
    }

}
