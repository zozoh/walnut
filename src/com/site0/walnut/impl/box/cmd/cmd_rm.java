package com.site0.walnut.impl.box.cmd;

import org.nutz.lang.Each;
import org.nutz.lang.Stopwatch;
import org.nutz.lang.Times;
import org.nutz.lang.util.Disks;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnQuery;
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
        int limit = params.getInt("limit", 0);

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
            // 准备父目录
            WnObj oP;

            // 修改通配符
            str = str.replace("*", ".*");

            // 如果有路径
            if (str.contains("/")) {
                int pos = str.lastIndexOf('/');
                // 不是从根目录开始
                if (pos > 0) {
                    String pPath = str.substring(0, pos);
                    String apPh = Wn.normalizeFullPath(pPath, sys);
                    oP = sys.io.check(oCurrent, apPh);
                }
                // 否则从根目录开始
                else {
                    oP = sys.io.getRoot();
                }

                // 得到名字
                str = str.substring(pos + 1);
            }
            // 否则就是当前目录
            else {
                oP = oCurrent;
            }

            // 如果直接就是 ID 的，那么删除它
            if (str.startsWith("id:")) {
                WnObj o = sys.io.checkById(str.substring(3));
                _do_delete(sys, isV, isR, isI, true, count[0], base, o);
                count[0]++;
            }
            // 否则设置查询条件
            else {
                WnQuery q = Wn.Q.pid(oP);
                if (limit > 0)
                    q.limit(limit);
                q.setv("nm", str);

                // 挨个查一下，然后删除
                sys.io.each(q, new Each<WnObj>() {
                    public void invoke(int index, WnObj o, int length) {
                        _do_delete(sys, isV, isR, isI, isH, count[0], base, o);
                        count[0]++;
                    }
                });
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
                              int index,
                              final String base,
                              WnObj o) {
        // 打印
        if (isV) {
            if (isI) {
                sys.out.printlnf("%d. %s", index, Disks.getRelativePath(base, o.path()));
            } else {
                sys.out.println(Disks.getRelativePath(base, o.path()));
            }
        }

        // 确保删除隐藏文件
        if (o.isHidden() && !isH) {
            return;
        }

        // 递归
        if (!o.isFILE() && !o.isLink() && isR) {
            sys.io.each(Wn.Q.pid(o.id()), new Each<WnObj>() {
                public void invoke(int index, WnObj child, int length) {
                    _do_delete(sys, isV, isR, isI, true, index, base, child);
                }
            });
        }
        // 删除自己
        sys.io.delete(o);
    }

}
