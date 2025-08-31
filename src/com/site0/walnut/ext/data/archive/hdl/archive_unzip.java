package com.site0.walnut.ext.data.archive.hdl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.nutz.lang.Files;
import org.nutz.lang.Streams;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.ext.data.archive.ArchiveContext;
import com.site0.walnut.ext.data.archive.ArchiveFilter;
import com.site0.walnut.ext.data.archive.api.ArchiveExtracting;
import com.site0.walnut.ext.data.archive.util.Archives;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

public class archive_unzip extends ArchiveFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(view|quiet)$");
    }

    @Override
    protected void process(WnSystem sys, ArchiveContext fc, ZParams params) {
        // 分析参数
        String fnm = fc.oArchive.name();
        String suffix = Files.getSuffixName(fnm);
        if (fnm.endsWith(".tar.gz")) {
            suffix = "tar.gz";
        }
        String type = params.getString("type", suffix);

        String cs = params.getString("charset", "UTF-8");
        Charset charset = Charset.forName(cs);

        fc.quiet = params.is("quiet");

        // 目标目录
        String taPath = params.val(0);
        WnObj oTargetDir = null;
        if (!Ws.isBlank(taPath)) {
            String aph = Wn.normalizeFullPath(taPath, sys);
            oTargetDir = sys.io.createIfNoExists(null, aph, WnRace.DIR);
        }
        final WnObj _dir = oTargetDir;

        // 读取压缩文件
        InputStream ins = sys.io.getInputStream(fc.oArchive);
        ArchiveExtracting ing = Archives.extract(type, ins, charset);
        try {
            // 写入到目的地
            if (null != _dir) {
                fc.count = ing.extract((i, en, _ins) -> {
                    WnRace race = en.getRace();
                    WnObj oF = sys.io
                        .createIfNoExists(_dir, en.getName(), race);
                    if (oF.isFILE()) {
                        sys.io.write(oF, _ins);
                    }
                    if (!fc.quiet) {
                        sys.out.printlnf("%3d) %s => %s", i, en, oF.path());
                    }
                });
            }
            // 阅览模式
            else {
                fc.count = ing.extract((i, en, _ins) -> {
                    if (!fc.quiet)
                        sys.out.printlnf("%3d) %s", i, en);
                });
            }
        }
        catch (IOException e) {
            throw Er.wrap(e);
        }

        finally {
            Streams.safeClose(ins);
        }
    }

}
