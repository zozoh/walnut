package com.site0.walnut.ext.net.ping;

import java.net.InetAddress;

import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class cmd_ping extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, null);
        String hostname = params.val_check(0);

        InetAddress inet = InetAddress.getByName(hostname);

        // 检查该地址是否可达，超时时间设置为 5000 毫秒
        boolean reach = inet.isReachable(5000);

        if (reach) {
            sys.out.println(hostname + " 是可达的.");
        } else {
            sys.out.println(hostname + " 不可达.");
        }

    }

}
