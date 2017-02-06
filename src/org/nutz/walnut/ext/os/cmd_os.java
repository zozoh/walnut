package org.nutz.walnut.ext.os;

import org.nutz.lang.OS;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class cmd_os extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, null);
        // 获取font列表
        if (params.is("fonts")) {
            String[] fonts = OS.fonts();
            for (String fnm : fonts) {
                sys.out.println(fnm);
            }
        }
        // 获取系统名称
        if (params.is("nm")) {
            sys.out.println(OS.OS_NAME);
        }
    }

}
