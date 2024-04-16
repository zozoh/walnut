package com.site0.walnut.ext.net.socket;

import java.net.InetSocketAddress;
import java.net.Socket;

import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class cmd_socket extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, null);
        String host = params.val_check(0);
        int port = params.val_check_int(1);
        int timeout = params.getInt("timeout", 1000);

        try {
            // 尝试建立到指定服务器和端口的连接
            Socket socket = new Socket();
            // 创建 socket 地址
            InetSocketAddress endpoint = new InetSocketAddress(host, port);
            // 尝试连接到远程地址，第二个参数是超时时间（以毫秒为单位）
            socket.connect(endpoint, timeout);
            // 如果没有抛出异常，说明连接成功，端口是可用的
            sys.out.printlnf("主机 %s 端口 %s 是可用的", host, port);
            // 关闭socket
            socket.close();
        }
        catch (Exception e) {
            // 异常处理，端口可能不可用或服务器不可达
            sys.out.printlnf("主机 %s 端口 %s 不可用或服务器不可达", host, port);
        }

    }

}
