package org.nutz.walnut.impl.box.cmd;

import org.nutz.lang.Times;
import org.nutz.walnut.impl.box.JvmBoxOutput;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class cmd_output extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) {
        // 看看是否需要延迟
        ZParams params = ZParams.parse(args, "tei");
        if (params.has("delay")) {
            long ms = params.getLong("delay");
            try {
                Thread.sleep(ms);
            }
            catch (InterruptedException e) {
                return;
            }
        }

        // 是否加入时间戳
        boolean t = params.is("t");
        boolean showIndex = params.is("i");

        // 要输出的信息
        String msg;

        // 有内容
        if (params.vals.length > 0) {
            StringBuilder sb = new StringBuilder();
            for (String v : params.vals) {
                // sb.append(Wn.normalizeStr(v, sys.se.vars())).append(' ');
                sb.append(v).append(' ');
            }
            if (sb.length() > 0)
                sb.deleteCharAt(sb.length() - 1);

            msg = sb.toString();
        }
        // 没内容，写空
        else {
            msg = t ? "" : "-no-msg-";
        }

        // 得到输出
        JvmBoxOutput jbo = params.is("e") ? sys.err : sys.out;

        // 看看是否需要多次输出
        int n = params.getInt("n", -1);

        // 输出的休息间隔（默认1s一个），最快不能超过1ms
        long inteval = Math.max(1L, params.getLong("interval", 1000L));

        try {
            // 计数
            int i = 0;
            // 无限循环输出
            if (0 == n) {
                while (true) {
                    __print(jbo, i++, msg, t, showIndex);
                    Thread.sleep(inteval);
                }
            }
            // 有限次数输出
            else {
                for (; i < n - 1; i++) {
                    __print(jbo, i, msg, t, showIndex);
                    Thread.sleep(inteval);
                }
                // 输出最后一条
                __print(jbo, i, msg, t, showIndex);
            }
        }
        catch (InterruptedException e) {}
    }

    protected void __print(JvmBoxOutput jbo, int i, String msg, boolean t, boolean showIndex) {
        String prefix = showIndex ? "" + i + ") " : "";
        if (!t) {
            jbo.printlnf("%s%s", prefix, msg);
        } else {
            jbo.printlnf("%s%s %s", prefix, Times.format("HH:mm:ss.SSS", Times.now()), msg);
        }
        jbo.flush();
    }
}
