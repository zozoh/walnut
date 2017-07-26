package org.nutz.walnut.ext.sshd.hdl;

import org.nutz.lang.random.R;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

@JvmHdlParamArgs("^(-n)$")
public class sshd_passwd implements JvmHdl {

    public void invoke(WnSystem sys, JvmHdlContext hc) {
        if (hc.params.vals.length > 0) {
            String password = hc.params.val_check(0).trim();
            if (password.length() < 10) {
                sys.err.print("ssh password at least 10 char");
                return;
            }
            WnObj wobj = sys.io.createIfNoExists(null,
                                                 Wn.normalizeFullPath("~/.ssh/token", sys),
                                                 WnRace.FILE);
            sys.io.writeText(wobj, password);
        } else {
            WnObj wobj = sys.io.fetch(null, Wn.normalizeFullPath("~/.ssh/token", sys));
            if (wobj == null || hc.params.is("n")) {
                wobj = sys.io.createIfNoExists(null,
                                               Wn.normalizeFullPath("~/.ssh/token", sys),
                                               WnRace.FILE);
                String password = R.UU32();
                sys.io.writeText(wobj, password);
                sys.out.print("ssh password : " + password);
            } else {
                sys.out.print("ssh password : " + sys.io.readText(wobj));
            }
        }
    }

}
