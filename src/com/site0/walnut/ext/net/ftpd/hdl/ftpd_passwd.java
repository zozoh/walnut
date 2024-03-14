package com.site0.walnut.ext.net.ftpd.hdl;

import org.nutz.lang.random.R;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;

@JvmHdlParamArgs("^(-n)$")
public class ftpd_passwd implements JvmHdl {

    public void invoke(WnSystem sys, JvmHdlContext hc) {
        if (hc.params.vals.length > 0) {
            String password = hc.params.val_check(0).trim();
            if (password.length() < 10) {
                sys.err.print("ftp password at least 10 char");
                return;
            }
            WnObj wobj = sys.io.createIfNoExists(null,
                                                 Wn.normalizeFullPath("~/.ftp/token", sys),
                                                 WnRace.FILE);
            sys.io.writeText(wobj, password);
        } else {
            WnObj wobj = sys.io.fetch(null, Wn.normalizeFullPath("~/.ftp/token", sys));
            if (wobj == null || hc.params.is("n")) {
                wobj = sys.io.createIfNoExists(null,
                                               Wn.normalizeFullPath("~/.ftp/token", sys),
                                               WnRace.FILE);
                String password = R.UU32();
                sys.io.writeText(wobj, password);
                sys.out.print("ftp password : " + password);
            } else {
                sys.out.print("ftp password : " + sys.io.readText(wobj));
            }
        }
    }

}
