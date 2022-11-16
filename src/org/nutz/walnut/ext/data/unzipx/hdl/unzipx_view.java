package org.nutz.walnut.ext.data.unzipx.hdl;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.ext.data.unzipx.UnzipxContext;
import org.nutz.walnut.ext.data.unzipx.UnzipxFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.ZParams;
import org.nutz.walnut.util.archive.WnArchiveEntry;
import org.nutz.walnut.util.archive.WnArchiveReading;
import org.nutz.walnut.util.archive.impl.WnZipArchiveReading;

public class unzipx_view extends UnzipxFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqn", "^(macosx|json|hidden)$");
    }

    @Override
    protected void process(WnSystem sys, UnzipxContext fc, ZParams params) {
        boolean hidden = params.is("hidden");
        boolean macosx = params.is("macosx");

        // 准备输入流
        InputStream ins = sys.io.getInputStream(fc.oZip, 0);

        // 准备读取方式
        WnArchiveReading ing = new WnZipArchiveReading(ins, fc.charset);

        List<WnArchiveEntry> list = new LinkedList<>();
        ing.onNext((en, zin) -> {
            // 判断一下隐藏文件
            if (!hidden && en.name.startsWith(".") || en.name.contains("/.")) {
                return;
            }
            // 判断一下 MACOS特殊文件夹
            if (!macosx && en.name.startsWith("__MACOSX") || en.name.contains("/__MACOSX")) {
                return;
            }
            list.add(en);
        });

        try {
            ing.readAll();
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
