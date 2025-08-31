package com.site0.walnut.ext.data.archive;

import java.nio.charset.Charset;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.impl.box.JvmFilterExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.ZParams;

public class cmd_archive
        extends JvmFilterExecutor<ArchiveContext, ArchiveFilter> {

    public cmd_archive() {
        super(ArchiveContext.class, ArchiveFilter.class);
    }

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqn");
    }

    @Override
    protected ArchiveContext newContext() {
        return new ArchiveContext();
    }

    @Override
    protected void prepare(WnSystem sys, ArchiveContext fc) {
        String ph = fc.params.val_check(0);
        fc.oArchive = Wn.checkObj(sys, ph);
        String charsetName = fc.params.getString("charset", "UTF-8");
        fc.charset = Charset.forName(charsetName);

        fc.hidden = fc.params.is("hidden");
        fc.macosx = fc.params.is("macosx");
    }

    @Override
    protected void output(WnSystem sys, ArchiveContext fc) {
        // 仅仅打印数量
        if (!fc.quiet) {
            sys.out.printlnf("Found %d Entries", fc.count);
        }
        // 那么就输出一个 JSON
        else {
            JsonFormat jfmt = Cmds.gen_json_format(fc.params);
            NutMap re = Wlang.map("count", fc.count);
            String json = Json.toJson(re, jfmt);
            sys.out.println(json);
        }
    }

}
