package com.site0.walnut.ext.net.mqttc;

import java.util.Arrays;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;

public class cmd_mqttc extends JvmHdlExecutor {

    @Override
    protected void _find_hdl_name(WnSystem sys, JvmHdlContext hc) {
        // 如果第一个参数就是处理器，那么，HOME 则自动寻找
        if (hc.args.length < 1) {
            throw Er.create("e.cmd.weixin.lackArgs", hc.args);
        }

        int pos;

        // 第一个参数就是 hdl，那么就不设置 home，可能有的命令不需要 Home
        if (null != this.getHdl(hc.args[0])) {
            hc.hdlName = hc.args[0];
            hc.oRefer = null;
            pos = 1;
        }
        // 第一个参数必须为微信公众号目录名
        else {
            if (hc.args.length < 2) {
                throw Er.create("e.cmd.mqttc.lackArgs", hc.args);
            }
            // 得到公众号
            String pnb = hc.args[0];

            // 表示当前目录为主目录
            if (".".equals(pnb)) {
                hc.oRefer = sys.getCurrentObj();
            }
            // 获取主目录
            else {
                String aph = Wn.normalizeFullPath("~/.mqttc/" + hc.args[0], sys);
                hc.oRefer = sys.io.check(null, aph);
            }

            if (hc.oRefer != null && hc.oRefer.isLink()) {
                String aph = Wn.normalizeFullPath("~/.mqttc/" + hc.oRefer.link(), sys);
                hc.oRefer = sys.io.check(null, aph);
            }

            // 获得处理器名称
            hc.hdlName = hc.args[1];

            // 处理参数的位置
            pos = 2;
        }

        // 解析参数
        hc.args = Arrays.copyOfRange(hc.args, pos, hc.args.length);
    }
}
