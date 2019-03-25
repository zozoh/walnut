package org.nutz.walnut.impl.box.cmd;

import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class cmd_man extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) {
        ZParams params = ZParams.parse(args, null);
        String lang = params.get("lang", "zh_cn");
        
        // 没参数，则用自己的说明
        if (params.vals.length == 0) {
            sys.out.println(this.getManual(lang));
        }
        // 否则用第一个参数作为要查看说明的命令
        else {
            String cmdName = params.val_check(0);
            String hdlName = params.val(1);
            JvmExecutor cmd = sys.jef.get(cmdName);
            // 没有命令
            if (null == cmd) {
                sys.err.printlnf("e.cmd.notfound : %s", cmdName);
            }
            // 获取帮助
            else{
                sys.out.println(cmd.getManual(hdlName, lang));
            }
        }
    }

}
