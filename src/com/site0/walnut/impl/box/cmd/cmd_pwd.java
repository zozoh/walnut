package com.site0.walnut.impl.box.cmd;

import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;

public class cmd_pwd extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) {
        String pwd = sys.session.getVars().getString("PWD");
        String aph = Wn.normalizeFullPath(pwd, sys);
        sys.out.println(aph);
    }

}
