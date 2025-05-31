package com.site0.walnut.impl.box.cmd;

import java.net.URLDecoder;

import org.nutz.lang.Strings;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class cmd_str extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, "n", "^(trim|replace)$");

        // 得到输入
        String str = sys.in.readAll();

        // 转换
        String s2 = str;
        if (params.has("decodeURI")) {
            String enc = params.getString("decodeURI", "UTF-8");
            s2 = URLDecoder.decode(s2, enc);
        }
        // 去掉前后空白
        if (params.has("trim")) {
            s2 = Strings.trim(s2);
        }
        // 去掉指定字符（正则表达）
        if (params.has("replace")) {
            String regex = params.val(0);
            String targs = params.val(1, "");
            s2 = s2.replaceAll(regex, targs);
        }

        // 输出
        if (params.is("n")) {
            sys.out.println(s2);
        } else {
            sys.out.print(s2);
        }
    }

}
