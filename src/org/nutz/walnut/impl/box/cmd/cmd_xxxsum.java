package org.nutz.walnut.impl.box.cmd;

import java.io.InputStream;

import org.nutz.lang.Lang;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public abstract class cmd_xxxsum extends JvmExecutor {

    protected String algorithm;

    public cmd_xxxsum(String algorithm) {
        this.algorithm = algorithm;
    }

    public void exec(WnSystem sys, String[] args) {
        // 文件输入
        if (args.length == 1) {
            String ph = Wn.normalizeFullPath(args[0], sys);
            WnObj o = sys.io.check(null, ph);
            InputStream ins = sys.io.getInputStream(o, 0);
            String _sum = sum(ins);
            sys.out.writeLine(_sum);
        }
        // 多个文件输入
        else if (args.length > 1) {
            for (String val : args) {
                String ph = Wn.normalizeFullPath(val, sys);
                WnObj o = sys.io.check(null, ph);
                InputStream ins = sys.io.getInputStream(o, 0);
                String _sum = sum(ins);
                sys.out.writeLinef("%s : %s(%s)", _sum, algorithm.toUpperCase(), val);
            }
        }
        // 如果有管道输入
        else if (sys.pipeId > 0) {
            String str = sys.in.readAll();
            if (null == str)
                sys.out.writeLine("!NO DATA");
            else
                sys.out.writeLine(sum(str));
        }
        // 否则
        else {
            sys.err.writeLine("!no input!");
        }
    }

    protected String sum(CharSequence cs) {
        return Lang.digest(algorithm, cs);
    }

    protected String sum(InputStream ins) {
        return Lang.digest(algorithm, ins);
    }
}
