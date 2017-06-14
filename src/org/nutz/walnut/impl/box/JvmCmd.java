package org.nutz.walnut.impl.box;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;

class JvmCmd {

    String cmdName;

    String[] args;

    String redirectPath;

    boolean redirectAppend;

    boolean redirectErrToStd;

    public JvmCmd(String src) {
        // 拆分
        String[] ss = Strings.split(src, false, ' ', '\t');

        // 不可能
        if (null == ss || ss.length == 0) {
            throw Lang.impossible();
        }

        // 记录命令名称
        cmdName = ss[0];

        // 只有一个
        int len = ss.length;
        if (len == 1) {
            args = new String[0];
            return;
        }

        // 最后一个是错误输出重定向到标准输出
        int li = len - 1;
        String s = ss[li];
        if (s.equals("2>&1")) {
            this.redirectErrToStd = true;
            len--;
        }

        // 检查默认重定向格式
        if (len > 2) {
            // 倒数第二个是个重定向
            li = len - 2;
            s = ss[li];
            // 重定向 : 覆盖
            if (s.equals(">")) {
                redirectPath = ss[li + 1];
                redirectAppend = false;
                args = new String[len - 3];
                System.arraycopy(ss, 1, args, 0, args.length);
                return;
            }
            // 重定向 : 追加
            else if (s.equals(">>")) {
                redirectPath = ss[li + 1];
                redirectAppend = true;
                args = new String[len - 3];
                System.arraycopy(ss, 1, args, 0, args.length);
                return;
            }
        }
        // 不重定向
        args = new String[len - 1];
        System.arraycopy(ss, 1, args, 0, args.length);
    }
}
