package org.nutz.walnut.ext.media.ooml.hdl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Streams;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.media.ooml.OomlContext;
import org.nutz.walnut.ext.media.ooml.OomlFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.ooml.OomlEntry;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;
import org.nutz.walnut.util.archive.WnArchiveWriting;
import org.nutz.walnut.util.archive.impl.WnZipArchiveWriting;

public class ooml_pack extends OomlFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqn", "^(quiet)$");
    }

    @Override
    protected void process(WnSystem sys, OomlContext fc, ZParams params) {
        // 分析参数
        JsonFormat jfmt = Cmds.gen_json_format(params);
        boolean quiet = params.is("quiet");

        // 准备目标输出文件
        String taPh = params.val_check(0);
        String aph = Wn.normalizeFullPath(taPh, sys);
        WnObj oZip = sys.io.createIfNoExists(null, aph, WnRace.FILE);

        OutputStream ops = null;
        WnArchiveWriting ag = null;

        try {
            // 准备输出流
            ops = sys.io.getOutputStream(oZip, 0);
            ag = new WnZipArchiveWriting(ops);

            // 逐个写入条目
            List<OomlEntry> list = fc.ooml.getEntries();
            for (OomlEntry en : list) {
                String rph = en.getPath();
                byte[] bs = en.getContent();
                ag.addFileEntry(rph, bs);
            }
        }
        catch (IOException e) {
            throw Er.wrap(e);
        }
        // 确保写入
        finally {
            Streams.safeFlush(ag);
            Streams.safeClose(ag);
            Streams.safeClose(ops);
        }

        // 搞定
        if (!quiet) {
            String json = Json.toJson(oZip, jfmt);
            sys.out.println(json);
        }
    }

}