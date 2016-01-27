package org.nutz.walnut.ext.bp;

import java.util.Arrays;

import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public class cmd_bp extends JvmHdlExecutor {

    @Override
    protected void setupContext(WnSystem sys, JvmHdlContext hc) {
        // 如果第一个参数就是处理器，那么，HOME 则自动寻找
        if (hc.args.length < 1) {
            throw Er.create("e.cmd.bp.lackArgs", hc.args);
        }

        int pos;
        if (null != this.getHdl(hc.args[0])) {
            hc.hdlName = hc.args[0];
            pos = 1;

            // 那么开始猜猜吧，哪个目录是主目录，有 site.json 的目录就是主目录
            WnObj oH = this.getHome(sys);
            WnObj oC = this.getCurrentObj(sys);
            WnObj o = oC;
            while (o.hasParent() && !o.isSameId(oH.id())) {
                if (sys.io.exists(o, "site.json")) {
                    hc.oHome = o;
                    break;
                }
                o = o.parent();
            }

            // 还是木有主目录
            if (null == hc.oHome) {
                throw Er.create("e.cmd.bp.nohome", oC.path());
            }

        }
        // 第一个参数必须为主目录
        else {
            if (hc.args.length < 2) {
                throw Er.create("e.cmd.bp.lackArgs", hc.args);
            }
            // 获取主目录
            hc.oHome = Wn.checkObj(sys, hc.args[0]);

            // 获得处理器名称
            hc.hdlName = hc.args[1];

            // 处理参数的位置
            pos = 2;
        }

        // 解析参数
        String[] args = Arrays.copyOfRange(hc.args, pos, hc.args.length);
        hc.params = ZParams.parse(args, null);
        
        hc.jfmt = this.gen_json_format(hc.params);

    }

}
