package org.nutz.walnut.impl.box.cmd;

import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

/**
 * 转换图片
 * 
 * @author pw
 *
 */
public class cmd_chimg extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        // 分析参数
        final ZParams params = ZParams.parse(args, null);
        // -i 输入
        String pa_i = params.check("i");
        // -o 输出
        String pa_o = params.check("o");
        // -s 大小
        String pa_s = params.get("s");
        // -p 类型
        System.out.println(String.format("i %s, o %s, s %s", pa_i, pa_o, pa_s));
    }

}
