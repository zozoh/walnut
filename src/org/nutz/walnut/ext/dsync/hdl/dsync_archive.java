package org.nutz.walnut.ext.dsync.hdl;

import org.nutz.walnut.api.WnOutputable;
import org.nutz.walnut.ext.dsync.DSyncContext;
import org.nutz.walnut.ext.dsync.DSyncFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class dsync_archive extends DSyncFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "fq", "^(force|quiet)$");
    }

    @Override
    protected void process(WnSystem sys, DSyncContext fc, ZParams params) {
        // 分析参数
        boolean force = params.is("force", "f");
        boolean quiet = params.is("quiet", "q");

        // 如果上下文木有加载树，则无视
        if (!fc.hasTrees()) {
            return;
        }

        // 准备输出日志
        WnOutputable log = null;
        if (!quiet) {
            log = sys.out;
        }

        // 确保加载了对象
        fc.api.loadTreesItems(fc.trees);

        // 生成归档
        fc.oArchive = fc.api.genArchive(fc.config, fc.trees, force, log);

    }

}
