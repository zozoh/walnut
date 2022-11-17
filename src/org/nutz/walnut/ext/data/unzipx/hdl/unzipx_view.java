package org.nutz.walnut.ext.data.unzipx.hdl;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Encoding;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.ext.data.unzipx.UnzipxContext;
import org.nutz.walnut.ext.data.unzipx.UnzipxFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.ZParams;
import org.nutz.walnut.util.archive.WnArchiveEntry;
import org.nutz.walnut.util.archive.WnArchiveReading;

public class unzipx_view extends UnzipxFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqn", "^(json)$");
    }

    @Override
    protected void process(WnSystem sys, UnzipxContext fc, ZParams params) {
        // 准备读取方式
        List<WnArchiveEntry> list = new LinkedList<>();
        WnArchiveReading ing = fc.openReading((en, zin) -> {
            list.add(en);
        }, null);

        try {
            ing.readAll();
        }
        // 采用 gbk 再试试
        catch (IllegalArgumentException e) {
            WnArchiveReading ing2 = null;
            if (fc.charset.displayName().equals("GBK")) {
                Charset cs = Encoding.CHARSET_UTF8;
                ing2 = fc.openReading((en, zin) -> {
                    list.add(en);
                }, cs);
            } else if (fc.charset.displayName().equals("UTF-8")) {
                Charset cs = Encoding.CHARSET_GBK;
                ing2 = fc.openReading((en, zin) -> {
                    list.add(en);
                }, cs);
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

        if (params.is("json")) {
            JsonFormat jfmt = Cmds.gen_json_format(params);
            String json = Json.toJson(list, jfmt);
            sys.out.println(json);
        }
        // 直接输出
        else {
            for (WnArchiveEntry en : list) {
                sys.out.println(en.toString());
            }
        }

    }

}
