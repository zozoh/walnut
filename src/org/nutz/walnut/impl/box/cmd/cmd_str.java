package org.nutz.walnut.impl.box.cmd;

import java.net.URLDecoder;

import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class cmd_str extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, null);

        // 得到输入
        String str = sys.in.readAll();

        // 转换
        if (params.has("decodeURI")) {
            String enc = params.getString("decodeURI", "UTF-8");
            sys.out.print(URLDecoder.decode(str, enc));
        }
        // 默认原样输出
        else {
            sys.out.print(str);
        }

    }

}
