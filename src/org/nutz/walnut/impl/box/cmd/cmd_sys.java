package org.nutz.walnut.impl.box.cmd;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnSysConf;
import org.nutz.walnut.util.WnSysRuntime;
import org.nutz.walnut.util.ZParams;

public class cmd_sys extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, "cqn");
        JsonFormat jfmt = Cmds.gen_json_format(params);

        // 获取运行时信息
        if ("runtime".equals(params.val(0))) {
            WnSysRuntime rt = Wn.getRuntime();
            sys.out.println(Json.toJson(rt, jfmt));
        }
        // 默认获取全局配置信息
        else {
            WnSysConf conf = Wn.getSysConf(sys.io);
            sys.out.println(Json.toJson(conf, jfmt));
        }
    }

}
