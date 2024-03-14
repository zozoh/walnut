package com.site0.walnut.ext.sys.hookx.hdl;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.sys.hookx.HookXContext;
import com.site0.walnut.ext.sys.hookx.HookXFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.WnContext;
import com.site0.walnut.util.ZParams;

public class hookx_invoke extends HookXFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "v");
    }

    @Override
    protected void process(WnSystem sys, HookXContext fc, ZParams params) {
        String[] actions = params.vals;
        if (null == actions || actions.length == 0)
            return;

        boolean verbose = params.is("v");

        // 列 hook
        WnContext wc = Wn.WC();
        for (WnObj o : fc.objs) {
            if (verbose) {
                sys.out.printlnf("OBJ: %s", o.getFormedPath(true));
            }
            for (String action : actions) {
                if (verbose) {
                    sys.out.printlnf("@%s:", action);
                    sys.out.printlnf("  redo hook -> %s", o.path());
                }
                wc.doHook(action, o);
            }
            if (verbose) {
                sys.out.println();
            }
        }
    }

}
