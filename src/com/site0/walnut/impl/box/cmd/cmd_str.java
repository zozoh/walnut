package com.site0.walnut.impl.box.cmd;

import java.net.URLDecoder;

import org.nutz.lang.Strings;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class cmd_str extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, "n", "^(trim)$");

        // 得到输入
        String str = sys.in.readAll();

        // 转换
        String s2;
        if (params.has("decodeURI")) {
            String enc = params.getString("decodeURI", "UTF-8");
            s2 = URLDecoder.decode(str, enc);
        }
        // 去掉空白
        else if (params.has("trim")) {
            s2 = Strings.trim(str);
        }
        // 默认原样输出
        else {
            s2 = str;
        }

        // 输出
        if (params.is("n")) {
            sys.out.println(s2);
        } else {
            sys.out.print(s2);
        }
    }

}
