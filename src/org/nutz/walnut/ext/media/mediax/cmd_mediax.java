package org.nutz.walnut.ext.media.mediax;

import java.util.Arrays;

import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public class cmd_mediax extends JvmHdlExecutor {

    @Override
    protected void _find_hdl_name(WnSystem sys, JvmHdlContext hc) {
        // 记录从哪里 copy args 的位置
        int pos;

        // 没有参数
        if (hc.args.length == 0) {
            throw Er.create("e.cmd.mediax.lackArgs", hc.args);
        }
        // 第一个参数就是 hdl，那么当前目录就作为 oHome
        // :> mediax hdlName xxx
        else if (null != this.getHdl(hc.args[0])) {
            hc.oRefer = Wn.getObj(sys, "~/.mediax");
            hc.hdlName = hc.args[0];
            pos = 1;
        }
        // 第一个参数表示一个 Account 并且有多余一个的参数
        // :> mediax [account] hdlName xxx
        else if (hc.args.length >= 2) {
            hc.oRefer = Wn.getObj(sys, "~/.mediax");
            hc.put("account", hc.args[0]);
            hc.hdlName = hc.args[1];
            pos = 2;
        }
        // 否则还是缺参数
        else {
            throw Er.create("e.cmd.mediax.lackArgs", hc.args);
        }

        // 检查 oHome，如果又不是 ThingSet 又不是 Thing，抛错
        // if (!hc.oHome.isType("thing") && !hc.oHome.has("thing")) {
        // throw Er.create("e.cmd.thing.invalidHome", hc.oHome);
        // }

        // Copy 剩余参数
        hc.args = Arrays.copyOfRange(hc.args, pos, hc.args.length);

    }

}
