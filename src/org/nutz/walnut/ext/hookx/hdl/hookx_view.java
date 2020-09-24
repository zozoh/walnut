package org.nutz.walnut.ext.hookx.hdl;

import java.util.List;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.walnut.api.hook.WnHook;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.hookx.HookXContext;
import org.nutz.walnut.ext.hookx.HookXFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public class hookx_view extends HookXFilter {

    private static final String[] ACTIONS = Lang.array("create",
                                                       "delete",
                                                       "meta",
                                                       "mount",
                                                       "move",
                                                       "write");

    @Override
    protected void process(WnSystem sys, HookXContext fc, ZParams params) {
        // 获取动作列表
        String[] actions = params.vals;
        if (null == params.vals || params.vals.length == 0) {
            actions = ACTIONS;
        }

        // 列 hook
        for (WnObj o : fc.objs) {
            sys.out.printlnf("OBJ: %s", o.getFormedPath(true));
            for (String action : actions) {
                List<WnHook> hooks = Wn.WC().getHooks(action, o);
                sys.out.printlnf("  @%-10s: %d hooks :", action, hooks.size());
                for (WnHook hook : hooks) {
                    sys.out.printlnf("    > %s", hook.toString());
                }
            }
            sys.out.println();
        }

        // 汇总
        sys.out.println(Strings.dup('-', 60));
        sys.out.printlnf("%d objs", fc.objs.size());
    }

}
