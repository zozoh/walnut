package org.nutz.walnut.ext.data.unzipx.hdl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Encoding;
import org.nutz.lang.Files;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.data.unzipx.UnzipxContext;
import org.nutz.walnut.ext.data.unzipx.UnzipxFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.ZParams;
import org.nutz.walnut.util.archive.WnArchiveEntry;
import org.nutz.walnut.util.archive.WnArchiveReading;
import org.nutz.walnut.util.archive.WnArchiveReadingCallback;

public class unzipx_export extends UnzipxFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "vcqn", "^(flat|quiet)$");
    }

    @Override
    protected void process(WnSystem sys, UnzipxContext fc, ZParams params) {
        boolean flat = params.is("flat");
        boolean quiet = params.is("quiet");
        boolean verbose = params.is("v");
        int buf_size = params.getInt("buf", 8192);
        JsonFormat jfmt = Cmds.gen_json_format(params);
        // 得到目标
        String ph = params.val_check(0);
        WnObj oTa = Wn.checkObj(sys, ph);
        if (!oTa.isDIR()) {
            throw Er.create("e.cmd.unzip.exportNoDir");
        }

        // 分析元数据
        NutMap metas = null;
        if (params.has("metas")) {
            String str = Cmds.checkParamOrPipe(sys, params, "metas", true);
            if (Ws.isQuoteBy(str, '{', '}')) {
                metas = Json.fromJson(NutMap.class, str);
            }
            // 来自文件
            else {
                WnObj o = Wn.checkObj(sys, str);
                metas = sys.io.readJson(o, NutMap.class);
            }
        }

        if (verbose) {
            sys.out.printlnf("export %s :", fc.oZip.name());
        }

        // 准备回调
        byte[] buf = new byte[buf_size];
        NutBean metaBeans = metas;
        List<WnObj> list = new LinkedList<>();
        int[] II = new int[1];
        II[0] = 1;
        WnArchiveReadingCallback callback = new WnArchiveReadingCallback() {
            public void invoke(WnArchiveEntry en, InputStream zin) throws IOException {
                NutMap meta = null;
                String nm = en.name;
                // 有元数据映射
                if (null != metaBeans) {
                    meta = metaBeans.getAs(en.name, NutMap.class);
                    // 没有映射，就无视
                    if (null == meta) {
                        return;
                    }
                    nm = meta.getString("nm", nm);
                    meta.remove("nm");
                }

                // 确保flat
                if (flat) {
                    nm = Files.getName(nm);
                }
                if (verbose) {
                    sys.out.printlnf("%d). %s -> %s", II[0]++, en.name, nm);
                }
                // 創建文件
                WnObj o = null;
                if (en.dir) {
                    o = sys.io.createIfNoExists(oTa, nm, WnRace.DIR);
                    if (null != meta && !meta.isEmpty()) {
                        sys.io.appendMeta(o, meta);
                    }
                } else {
                    o = sys.io.createIfNoExists(oTa, nm, WnRace.FILE);
                    if (null != meta && !meta.isEmpty()) {
                        sys.io.appendMeta(o, meta);
                    }
                    // 写入流
                    OutputStream ops = sys.io.getOutputStream(o, 0);
                    en.writeAndClose(zin, ops, buf);
                }
                if (null != o) {
                    list.add(o);
                }
            }
        };

        // 开始解压
        WnArchiveReading ing = fc.openReading(callback, null);
        try {
            ing.readAll();
        }
        // 采用 gbk 再试试
        catch (IllegalArgumentException e) {
            WnArchiveReading ing2 = null;
            if (fc.charset.displayName().equals("GBK")) {
                Charset cs = Encoding.CHARSET_UTF8;
                ing2 = fc.openReading(callback, cs);
            } else if (fc.charset.displayName().equals("UTF-8")) {
                Charset cs = Encoding.CHARSET_GBK;
                ing2 = fc.openReading(callback, cs);
            }
            if (null != ing2) {
                try {
                    ing2.readAll();
                }
                catch (IOException e1) {
                    throw Er.create("e.cmd.unzipx", e);
                }
            }
        }
        catch (IOException e) {
            throw Er.create("e.cmd.unzipx", e);
        }

        // 输出
        if (!quiet) {
            String json = Json.toJson(list, jfmt);
            sys.out.println(json);
        }

    }

}
