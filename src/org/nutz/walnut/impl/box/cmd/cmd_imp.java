package org.nutz.walnut.impl.box.cmd;

import org.nutz.walnut.api.io.WnImpExp;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class cmd_imp extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, "^(debug|v|trace|keep|dry)$");
        WnImpExp impexp = ioc.get(WnImpExp.class);
        impexp.imp(params.val_check(0), params.val_check(1), params, sys.getLog(params));
    }
}
