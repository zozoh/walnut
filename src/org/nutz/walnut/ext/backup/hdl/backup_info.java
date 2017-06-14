package org.nutz.walnut.ext.backup.hdl;

import java.io.IOException;
import java.io.InputStream;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.backup.BackupPackage;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

@JvmHdlParamArgs("^(debug|v|trace|keep|dry)$")
public class backup_info extends backup_xxx implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        String path = Wn.normalizeFullPath(hc.params.val_check(0), sys);
        WnObj wobj = sys.io.check(null, path);
        try (InputStream ins = sys.io.getInputStream(wobj, 0)) {
            BackupPackage bp = readBackupZip(ins);
            if (wobj.containsKey("backup_config")) {
                sys.out.print("config: ");
                sys.out.writeJson(wobj.get("backup_config"));
                sys.out.println();
            }
            sys.out.println("size : " + wobj.len());
            sys.out.println("obj count : " + bp.lines.size());
            sys.out.println("bucket count : " + bp.sha1Set.size());
        }
        catch (IOException e) {
            sys.err.print("something happen : " + e.getMessage());
        }
    }

}
