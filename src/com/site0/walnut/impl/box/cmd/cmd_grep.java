package com.site0.walnut.impl.box.cmd;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.nutz.lang.Streams;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

public class cmd_grep extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) {
        ZParams params = ZParams.parse(args, null);

        Pattern p = params.has("e") ? Pattern.compile(params.get("e")) : null;
        String pstr = params.vals.length > 0 ? params.vals[0] : null;

        if (p == null && pstr == null) {
            throw Er.create("e.cmd.grep.lackpattern");
        }

        // 从文件读取
        if (params.has("f")) {
            String filePath = params.get("f");

            // 计算要列出的要处理的对象
            List<WnObj> list = new LinkedList<WnObj>();
            Cmds.evalCandidateObjs(sys, Wlang.array(filePath), list, 0);

            // 检查是否候选对象列表为空
            Cmds.assertCandidateObjsNoEmpty(args, list);

            // 输出内容
            for (WnObj o : list) {
                // 目录不能输出
                if (o.isDIR()) {
                    sys.err.printlnf("e.io.readdir : %s", o.path());
                    continue;
                }
                InputStream ins = sys.io.getInputStream(o, 0);

                try {
                    BufferedReader br = Streams.buffr(new InputStreamReader(ins));
                    __do_grep(sys, p, pstr, br);
                }
                finally {
                    Streams.safeClose(ins);
                }
            }
        }
        // 从上一个命令输出读入
        else {
            __do_grep(sys, p, pstr, sys.in.getReader());
        }
    }

    protected void __do_grep(WnSystem sys, Pattern p, String pstr, BufferedReader br) {
        String line;
        try {
            while (null != (line = br.readLine())) {
                // 正则
                if (null != p) {
                    String trimed = Ws.trim(line);
                    if (p.matcher(trimed).find()) {
                        sys.out.println(line);
                    }
                }
                // 字符串
                else {
                    if (line.contains(pstr)) {
                        sys.out.println(line);
                    }
                }
            }
        }
        catch (IOException e) {
            throw Er.wrap(e);
        }
    }

}
