package com.site0.walnut.impl.box.cmd;

import java.io.InputStream;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Wsum;
import com.site0.walnut.util.ZParams;

public abstract class cmd_xxxsum extends JvmExecutor {

    protected String algorithm;

    public cmd_xxxsum(String algorithm) {
        this.algorithm = algorithm;
    }

    public void exec(WnSystem sys, String[] args) {
        ZParams params = ZParams.parse(args, "nt");
        // 文件输入
        if (params.vals.length == 1) {
            String ph = Wn.normalizeFullPath(params.vals[0], sys);
            WnObj o = sys.io.check(null, ph);
            InputStream ins = sys.io.getInputStream(o, 0);
            String _sum = sum(ins);
            sys.out.print(_sum);
        }
        // 多个文件输入
        else if (params.vals.length > 1) {
            for (String val : params.vals) {
                String ph = Wn.normalizeFullPath(val, sys);
                WnObj o = sys.io.check(null, ph);
                InputStream ins = sys.io.getInputStream(o, 0);
                String _sum = sum(ins);
                sys.out.printf("%s : %s(%s)", _sum, algorithm.toUpperCase(), val);
            }
        }
        // 字符串
        else if (params.has("s")) {
            String str = params.get("s");
            String _sum = sum(str);
            sys.out.print(_sum);
        }
        // 管道：读字符串去掉空白
        else if (params.is("t")) {
            String str = sys.in.readAll();
            String _sum = sum(str);
            sys.out.print(_sum);
        }
        // 默认就读管道吧
        else {
            InputStream ins = sys.in.getInputStream();
            String _sum = sum(ins);
            sys.out.print(_sum);
        }
        // 输出换行
        if (!params.is("n"))
            sys.out.println();
    }

    protected String sum(CharSequence cs) {
        return Wsum.digestAsString(algorithm, cs.toString());
    }

    protected String sum(InputStream ins) {
        return Wsum.digestAsString(algorithm, ins);
    }
}
