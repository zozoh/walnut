package org.nutz.walnut.impl.box.cmd;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

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
        // 如果有管道输入
        else if (sys.pipeId > 0) {
            InputStream ins = sys.in.getInputStream();
            if (params.is("t")) {
                String tmp = Streams.readAndClose(new InputStreamReader(ins));
                tmp = tmp.trim();
                ins = new ByteArrayInputStream(tmp.getBytes());
            }
            String _sum = sum(ins);
            sys.out.print(_sum);
        }
        // 字符串
        else if (params.has("s")) {
            String str = params.get("s");
            String _sum = sum(str);
            sys.out.print(_sum);
        }
        // 输出换行
        if (!params.is("n"))
            sys.out.println();
    }

    protected String sum(CharSequence cs) {
        return Lang.digest(algorithm, cs);
    }

    protected String sum(InputStream ins) {
        return Lang.digest(algorithm, ins);
    }
}
