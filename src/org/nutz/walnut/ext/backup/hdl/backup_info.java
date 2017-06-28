package org.nutz.walnut.ext.backup.hdl;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.backup.BackupPackage;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnPager;
import org.nutz.walnut.util.ZParams;

@JvmHdlParamArgs("^(debug|v|trace|keep|dry)$")
public class backup_info extends backup_xxx implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        ZParams params = ZParams.parse(hc.args, "iocnqhbslAVNPHQ", "^(mine|pager|ExtendDeeply|hide|list)$");
        String path = Wn.normalizeFullPath(params.val_check(0), sys);
        WnObj wobj = sys.io.check(null, path);
        BackupPackage bp = readBackupPackage(sys.io, path, params.is("list"));
        if (params.is("list")) {
            Cmds.output_objs(sys, params, new WnPager(params), bp.objs, true);
        } else {
            if (wobj.containsKey("backup_config")) {
                sys.out.print("config: ");
                sys.out.writeJson(wobj.get("backup_config"));
                sys.out.println();
            }
            sys.out.println("size : " + wobj.len());
            sys.out.println("obj count : " + bp.lines.size());
            sys.out.println("bucket count : " + bp.sha1Set.size());
        }
    }

}
