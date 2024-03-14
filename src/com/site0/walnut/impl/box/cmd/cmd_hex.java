package com.site0.walnut.impl.box.cmd;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.Streams;
import com.site0.walnut.api.WnOutputable;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.ZParams;

public class cmd_hex extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws IOException {
        ZParams params = ZParams.parse(args, null);

        // 得到宽度
        int width = params.getInt("w", 16);

        // 得到输出的字节范围
        long from = params.getLong("from", 0);
        long to = params.getLong("to", 0);

        // 对齐宽度
        from = from / width * width;

        // 从文件读取
        if (params.vals.length > 0) {

            // 计算要列出的要处理的对象
            List<WnObj> list = new LinkedList<WnObj>();
            Cmds.evalCandidateObjs(sys, params.vals, list, 0);

            // 检查是否候选对象列表为空
            Cmds.assertCandidateObjsNoEmpty(args, list);

            // 输出内容
            for (WnObj o : list) {
                // 目录不能输出
                if (o.isDIR()) {
                    sys.err.printlnf("e.io.readdir : %s", o.path());
                    continue;
                }
                InputStream ins = sys.io.getInputStream(o, from);

                // 多个输出，输出文件名
                if (list.size() > 1) {
                    sys.out.printlnf("FILE: %s:", o.name());
                }

                try {
                    __do_hex(sys.out, width, ins, from, 0, to);
                }
                finally {
                    Streams.safeClose(ins);
                }

                if (list.size() > 1) {
                    sys.out.println();
                }
            }
        }
        // 从上一个命令输出读入
        else {
            __do_hex(sys.out, width, sys.in.getInputStream(), 0, from, to);
        }
    }

    protected void __do_hex(WnOutputable out,
                            int width,
                            InputStream ins,
                            long read,
                            long from,
                            long to) throws IOException {
        byte[] bs = new byte[width];
        int len;
        while (-1 != (len = ins.read(bs))) {
            if (read >= from) {
                // 输出地址
                out.print(String.format("%07x:", read));

                // 输出字节
                int last = len - 1;
                for (int i = 0; i < len; i++) {
                    if (i < last) {
                        byte b = bs[i];
                        byte b2 = bs[++i];
                        out.printf(" %02x%02x", b, b2);
                    } else {
                        byte b = bs[i];
                        out.printf(" %02x", b);
                    }
                }
                // 换行
                out.println();

                // 计数
                read += len;

                // 退出
                if (to != 0 && read > to) {
                    break;
                }
            }
        }
    }
}
