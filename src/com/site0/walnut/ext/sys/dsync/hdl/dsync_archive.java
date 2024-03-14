package com.site0.walnut.ext.sys.dsync.hdl;

import com.site0.walnut.api.WnOutputable;
import com.site0.walnut.ext.sys.dsync.DSyncContext;
import com.site0.walnut.ext.sys.dsync.DSyncFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

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
