package org.nutz.walnut.ext.backup.hdl;

import java.util.ArrayList;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.backup.BackupRestoreContext;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

@JvmHdlParamArgs("^(debug|v|trace|keep|dry|ignore_sha1_miss|force_id|overwrite)$")
public class backup_restore extends backup_xxx implements JvmHdl {

    public void invoke(WnSystem sys, JvmHdlContext hc) {
        ZParams params = hc.params;
        BackupRestoreContext ctx = new BackupRestoreContext();
        ctx.sys = sys;
        ctx.log = sys.getLog(params);
        ctx.se = sys.session;

        ctx.main = Wn.normalizeFullPath(hc.params.val_check(0), sys);
        WnObj mainWnObj = sys.io.check(null, ctx.main);
        ctx.target = Wn.normalizeFullPath(params.check("target"), sys);
        if (!ctx.target.endsWith("/"))
            ctx.target += "/";
        

        ctx.dry = hc.params.is("dry");
        ctx.ignore_sha1_miss = hc.params.is("ignore_sha1_miss");
        ctx.force_id = hc.params.is("force_id");
        ctx.debug = hc.params.is("debug");
        ctx.overwrite = hc.params.is("overwrite");
        ctx.base = hc.params.get("base");
        if (ctx.base == null) {
            NutMap backup_config = mainWnObj.getAs("backup_config", NutMap.class);
            if (backup_config == null) {
                sys.err.print("备份文件没有backup_config,需要指定base参数!!");
            }
            ctx.base = backup_config.getString("base");
            if (ctx.base == null) {
                sys.err.print("备份文件有backup_config但没有base属性,需要指定base参数!!");
            }
        }

        ctx.prevs = new ArrayList<>();
        ctx.prevPackages = new ArrayList<>();
        if (hc.params.has("prevs")) {
            for (String path : hc.params.getString("prevs").split(";")) {
                path = Wn.normalizeFullPath(path, sys);
                ctx.prevs.add(path);
                try {
                    ctx.prevPackages.add(readBackupPackage(sys.io, path, false));
                }
                catch (Exception e) {
                    sys.err.print("bad package : " + path);
                    return;
                }
            }
        }
        restore(ctx);
    }
}
