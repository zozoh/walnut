package org.nutz.walnut.impl.box.cmd;

import org.nutz.walnut.api.io.WnImpExp;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class cmd_exp extends JvmExecutor {

    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, "^(debug|v|trace|keep|dry)$");
        String root = params.val_check(0);
        
        // 先暴力取一下吧
        WnImpExp impExp = ioc.get(WnImpExp.class);
        impExp.exp(root, params, sys.getLog(params));
    }

}
