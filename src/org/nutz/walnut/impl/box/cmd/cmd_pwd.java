package org.nutz.walnut.impl.box.cmd;

import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public class cmd_pwd extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) {
        String pwd = sys.session.getVars().getString("PWD");
        String aph = Wn.normalizeFullPath(pwd, sys);
        sys.out.println(aph);
    }

}
