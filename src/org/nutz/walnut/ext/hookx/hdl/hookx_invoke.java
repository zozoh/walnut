package org.nutz.walnut.ext.hookx.hdl;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.hookx.HookXContext;
import org.nutz.walnut.ext.hookx.HookXFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnContext;
import org.nutz.walnut.util.ZParams;

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
