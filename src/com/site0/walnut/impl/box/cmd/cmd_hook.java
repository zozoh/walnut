package com.site0.walnut.impl.box.cmd;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.hook.WnHook;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.ZParams;

public class cmd_hook extends JvmExecutor {

    private static final String[] acnms = Wlang.array("create",
                                                      "delete",
                                                      "meta",
                                                      "mount",
                                                      "move",
                                                      "write");

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, "v");

        // 列 hook
        if (params.has("get")) {
            WnObj o = Wn.checkObj(sys, params.get("get"));

            // 列出全部 hook
            if (params.vals.length == 0) {
                for (String action : acnms) {
                    sys.out.printlnf("@%s:", action);
                    _list_hook(sys, action, o);
                }
            } else {
                String action = _check_action_name(params);
                _list_hook(sys, action, o);
            }

        }
        // 重新执行 hook
        else if (params.has("do") && params.vals.length > 0) {
            String action = _check_action_name(params);
            String str = params.get("do");
            List<WnObj> objs = Cmds.evalCandidateObjs(sys, Wlang.array(str), 0);
            for (WnObj o : objs) {
                if (params.is("v"))
                    sys.out.printlnf("redo hook -> %s", o.path());
                Wn.WC().doHook(action, o);
            }
        }
        // 其他的就抛错
        else {
            throw Er.create("e.cmd.hook.invalid.params");
        }
    }

    private String _check_action_name(ZParams params) {
        String action = params.vals[0];
        if (Arrays.binarySearch(acnms, action, new Comparator<String>() {
            public int compare(String s1, String s2) {
                return s1.compareTo(s2);
            }
        }) < 0) {
            throw Er.create("e.cmd.hook.invalid.actionName", action);
        }
        return action;
    }

    private void _list_hook(WnSystem sys, String action, WnObj o) {
        List<WnHook> hooks = Wn.WC().getHooks(action, o);
        for (WnHook hook : hooks) {
            sys.out.printlnf(" - %s", hook.toString());
        }
    }

}
