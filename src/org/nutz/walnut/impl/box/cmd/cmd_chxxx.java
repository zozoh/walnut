package org.nutz.walnut.impl.box.cmd;

import java.util.Arrays;
import java.util.LinkedList;

import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

abstract class cmd_chxxx extends JvmExecutor {

    protected _ch_context _eval_params(WnSystem sys, String[] args) {
        ZParams params = ZParams.parse(args, "Rv");

        if (params.vals.length < 2) {
            throw Er.create("e.cmd.lackargs", this.getMyName());
        }

        // 一些开关和参数
        _ch_context cc = new _ch_context();
        cc.str = params.vals[0];
        cc.R = params.is("R");
        cc.v = params.is("v");

        // 得到路径
        cc.list = new LinkedList<WnObj>();
        String[] paths = Arrays.copyOfRange(params.vals, 1, params.vals.length);
        cc.current = evalCandidateObjsNoEmpty(sys, paths, cc.list, false);
        return cc;
    }

}
