package org.nutz.walnut.ext.hmaker;

import java.util.Arrays;

import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public class cmd_hmaker extends JvmHdlExecutor {

    @Override
    protected void _find_hdl_name(WnSystem sys, JvmHdlContext hc) {
        // 如果第一个参数就是处理器，那么，HOME 则自动寻找
        if (hc.args.length < 1) {
            throw Er.create("e.cmd.hmaker.lackArgs", hc.args);
        }

        int pos;
        
        // 第一个参数就是 hdl，那么主目录被认为是当前目录
        if (null != this.getHdl(hc.args[0])) {
            hc.hdlName = hc.args[0];
            hc.oRefer = sys.getCurrentObj();
            pos = 1;
        }
        // 第一个参数必须为主目录
        else {
            if (hc.args.length < 2) {
                throw Er.create("e.cmd.hmaker.lackArgs", hc.args);
            }
            // 获取主目录
            hc.oRefer = Wn.checkObj(sys, hc.args[0]);

            // 获得处理器名称
            hc.hdlName = hc.args[1];

            // 处理参数的位置
            pos = 2;
        }

        // 解析参数
        hc.args = Arrays.copyOfRange(hc.args, pos, hc.args.length);
    }

}
