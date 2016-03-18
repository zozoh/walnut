package org.nutz.walnut.impl.box.cmd;

import org.nutz.lang.random.R;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class cmd_random extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {

        ZParams params = ZParams.parse(args, "n");

        // 生成几位的随机数，默认 4
        int nb = params.getInt("s", 4);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nb; i++) {
            sb.append(R.random(0, 9));
        }

        if (params.is("n"))
            sb.append('\n');

        // 输出
        sys.out.print(sb);

    }

}
