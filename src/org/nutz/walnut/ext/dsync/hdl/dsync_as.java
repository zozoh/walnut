package org.nutz.walnut.ext.dsync.hdl;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.walnut.ext.dsync.DSyncContext;
import org.nutz.walnut.ext.dsync.DSyncFilter;
import org.nutz.walnut.ext.dsync.bean.WnDataSyncTree;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.ZParams;

public class dsync_as extends DSyncFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqn", "^(quiet)$");
    }

    @Override
    protected void process(WnSystem sys, DSyncContext fc, ZParams params) {
        boolean quiet = params.is("quiet");
        String mode = params.val(0, "brief");

        // 有时候，在 regapi 里，会直接写 @as，加一个开关可以让其失效比较有腾挪的余地
        if (quiet)
            return;

        String HR = Ws.repeat('-', 60);

        // 打印包配置信息
        String pkgName = fc.api.getPackageName(fc.config, fc.trees);
        sys.out.println(HR);
        sys.out.println("DSYNC PACKAGE:");
        sys.out.println(pkgName);

        // 打印详情
        JsonFormat jfmt = Cmds.gen_json_format(params);
        for (WnDataSyncTree tree : fc.trees) {
            sys.out.println(HR);
            sys.out.printlnf("- %s", tree.getName());
            sys.out.println(HR);

            String str;
            // 输出归档元数据
            if ("archive".equals(mode)) {
                str = Json.toJson(fc.oArchive, jfmt);
            }
            // 输出元数据
            else if ("metas".equals(mode)) {
                str = tree.toMetaString(jfmt);
            }
            // 输出索引树
            else if ("tree".equals(mode)) {
                str = tree.toString();
            }
            // 输出摘要
            else {
                str = tree.toBreif();
            }
            sys.out.println(str);
        }
    }

}
