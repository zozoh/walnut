package com.site0.walnut.impl.box.cmd;

import java.util.List;

import org.nutz.lang.Each;
import org.nutz.lang.Stopwatch;
import org.nutz.lang.Times;
import org.nutz.lang.util.Disks;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.ZParams;

public class cmd_rm extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) {
        ZParams params = ZParams.parse(args, "rfvIH");
        boolean isV = params.is("v");
        boolean isR = params.is("r");
        boolean isI = params.is("I");
        boolean isH = params.is("H");

        // limit
        //int limit = params.getInt("limit", 0);

        // 参数错误
        if (params.vals.length < 1) {
            throw Er.create("e.cmd.mv.lackargs");
        }

        // 得到当前的目录
        WnObj oCurrent = sys.getCurrentObj();
        String base = oCurrent.path();

        // 准备计数
        final int[] count = new int[1];
        Stopwatch sw = Stopwatch.begin();

        // 循环每个参数
        for (String str : params.vals) {
            // 修改通配符
            // str = str.replace("*", ".*");
            // 如果是有通配符，那么就是查询
            if (str.contains("*")) {
                String ph = Wn.normalizeFullPath(str, sys);
                int pos = str.lastIndexOf('/');
                String pph = null;
                String name = str;
                if (pos > 0) {
                    // 保留这个结尾的 "/" 因为对于 S3 等对象存储，需要用 "/" 区别是目录
                    pph = ph.substring(0, pos + 1);
                    name = ph.substring(pos + 1).trim();
                }
                WnObj oP = Wn.checkObj(sys, pph);
                List<WnObj> objs = sys.io.getChildren(oP, name);
                for (WnObj o : objs) {
                    _do_delete(sys, isV, isR, isI, isH, count, base, o);
                }
            }
            // 否则就是直接删除
            else {
                WnObj o = Wn.checkObj(sys, str);
                _do_delete(sys, isV, isR, isI, isH, count, base, o);

            }
        }

        // 最后打印结束
        sw.stop();
        if (isV) {
            long du = sw.getDuration();
            String ts = Times.sT((int) du / 1000);
            sys.out.printlnf("%d obj deleted in %s (%sms)", count[0], ts, du);
        }

    }

    protected void _do_delete(final WnSystem sys,
                              final boolean isV,
                              final boolean isR,
                              final boolean isI,
                              final boolean isH,
                              int[] count,
                              final String base,
                              WnObj o) {
        int index = count[0];
        // 打印
        if (isV) {
            if (isI) {
                sys.out.printlnf("%d. %s",
                                 index,
                                 Disks.getRelativePath(base, o.path()));
            } else {
                sys.out.println(Disks.getRelativePath(base, o.path()));
            }
        }

        // 确保删除隐藏文件
        if (o.isHidden() && !isH) {
            return;
        }

        // 递归
        if (!o.isMountEntry()) {
            if (!o.isFILE() && !o.isLink() && isR) {
                sys.io.each(Wn.Q.pid(o.id()), new Each<WnObj>() {
                    public void invoke(int index, WnObj child, int length) {
                        _do_delete(sys,
                                   isV,
                                   isR,
                                   isI,
                                   true,
                                   count,
                                   base,
                                   child);
                    }
                });
            }
        }
        // 删除自己
        sys.io.delete(o);
        count[0]++;
    }

}
