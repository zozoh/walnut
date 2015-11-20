package org.nutz.walnut.impl.box;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;

class JvmCmd {

    String cmdName;

    String[] args;

    String redirectPath;

    boolean redirectAppend;

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
        if (ss.length == 1) {
            args = new String[0];
            return;
        }

        // 倒数第二个是个重定向
        int li = ss.length - 2;
        String s = ss[li];
        // 重定向 : 覆盖
        if (s.equals(">")) {
            redirectPath = ss[li + 1];
            redirectAppend = false;
            args = new String[ss.length - 3];
            System.arraycopy(ss, 1, args, 0, args.length);
        }
        // 重定向 : 追加
        else if (s.equals(">>")) {
            redirectPath = ss[li + 1];
            redirectAppend = true;
            args = new String[ss.length - 3];
            System.arraycopy(ss, 1, args, 0, args.length);
        }
        // 不重定向
        else {
            args = new String[ss.length - 1];
            System.arraycopy(ss, 1, args, 0, args.length);
        }

    }
}
