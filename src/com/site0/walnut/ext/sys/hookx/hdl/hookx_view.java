package com.site0.walnut.ext.sys.hookx.hdl;

import java.util.List;

import com.site0.walnut.util.Wlang;
import org.nutz.lang.Strings;
import com.site0.walnut.api.hook.WnHook;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.sys.hookx.HookXContext;
import com.site0.walnut.ext.sys.hookx.HookXFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.ZParams;

public class hookx_view extends HookXFilter {

    private static final String[] ACTIONS = Wlang.array("create",
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
