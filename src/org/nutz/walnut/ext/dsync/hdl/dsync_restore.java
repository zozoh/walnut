package org.nutz.walnut.ext.dsync.hdl;

import org.nutz.lang.Stopwatch;
import org.nutz.walnut.api.WnOutputable;
import org.nutz.walnut.ext.dsync.DSyncContext;
import org.nutz.walnut.ext.dsync.DSyncFilter;
import org.nutz.walnut.ext.dsync.bean.WnRestoreSettings;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;
import org.nutz.walnut.util.archive.WnArchiveSummary;

public class dsync_restore extends DSyncFilter {

    @Override
    protected void process(WnSystem sys, DSyncContext fc, ZParams params) {
        WnRestoreSettings settings = new WnRestoreSettings();
        settings.quiet = params.is("quiet", "q");
        settings.force = params.is("force", "f");
        settings.run = sys;

        // 木有可换原的项目
        if (!fc.hasTrees()) {
            return;
        }

        // 准备输出日志
        WnOutputable log = null;
        if (!settings.quiet) {
            log = sys.out;
        }

        // 准备输出目录
        String aph = Wn.normalizeFullPath("~/.dsync/data/", sys);
        settings.oDataHome = sys.io.fetch(null, aph);

        // 准备计时
        Stopwatch sw = Stopwatch.begin();

        // 逐棵树还原
        WnArchiveSummary sum = fc.api.restore(fc.config, fc.trees, settings, log);

        // 停止计时
        sw.stop();

        // 打印结果
        if (!settings.quiet) {
            sys.out.printlnf("Done for restore %s in %s", fc.oArchive, sw.toString());
            sys.out.println(sum.toString());
        }
    }

}
