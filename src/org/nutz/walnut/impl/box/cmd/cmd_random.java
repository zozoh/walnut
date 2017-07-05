package org.nutz.walnut.impl.box.cmd;

import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class cmd_random extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {

        ZParams params = ZParams.parse(args, "n");
        StringBuilder sb = new StringBuilder();

        // 固定数量模式
        if (params.has("s")) {
            // 生成几位的随机数，默认 4
            int nb = params.getInt("s", 4);
            for (int i = 0; i < nb; i++) {
                sb.append(R.random(0, 9));
            }
        }
        // 整数范围
        else if (params.vals.length > 0) {
            String[] ss = Strings.splitIgnoreBlank(params.val(0));
            int min = Integer.parseInt(ss[0]);
            int max = Integer.parseInt(ss[1]);
            sb.append(R.random(min, max));
        }
        // 默认是一个浮点数
        else {
            sb.append(R.get().nextFloat());
        }

        // 输出
        sys.out.print(sb);

        // 输出空行
        if (params.is("n"))
            sys.out.println();

    }

}
