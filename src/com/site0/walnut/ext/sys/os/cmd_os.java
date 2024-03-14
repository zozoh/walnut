package com.site0.walnut.ext.sys.os;

import org.nutz.lang.OS;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class cmd_os extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, null);
        // 获取font列表
        if (params.is("fonts")) {
            String[] fonts = OS.fontsRefresh();
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
