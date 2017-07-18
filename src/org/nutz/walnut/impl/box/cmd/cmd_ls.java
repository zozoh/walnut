package org.nutz.walnut.impl.box.cmd;

import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.Each;
import org.nutz.lang.Lang;
import org.nutz.lang.util.Disks;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnObjTable;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public class cmd_ls extends JvmExecutor {

    @Override
    public void exec(final WnSystem sys, String[] args) {
        // 分析参数
        final ZParams params = ZParams.parse(args, "lhAi");
        // TODO 搞搞参数...

        // 计算要列出的目录并得到当前目录
        List<WnObj> list = new LinkedList<WnObj>();
        WnObj p = evalCandidateObjs(sys, params.vals, list, Wn.Cmd.JOIN_CURRENT);

        // 检查是否候选对象列表为空
        checkCandidateObjsNoEmpty(args, list);

        // 准备要显示的表格
        String keys = "";
        if (params.is("l")) {
            keys = "md,c,g,len,lm";
            if (params.is("i"))
                keys = "id," + keys;
        }
        final boolean useColor = sys.nextId < 0;
        final boolean briefSize = params.is("h");
        final boolean showHidden = params.is("A");
        final int maxN = 100; // 最多显示记录的个数

        // 只有一个内容
        if (list.size() == 1) {
            final WnObjTable tab = new WnObjTable(keys);
            WnObj o = list.get(0);
            boolean tooMany = false;
            // 本身就是文件
            if (o.isFILE()) {
                tab.add(o, useColor, briefSize);
            }
            // 是个目录
            else {
                tooMany = __joint_children(sys, useColor, briefSize, showHidden, maxN, tab, o);
            }
            sys.out.print(tab.toString());
            if (tooMany) {
                sys.out.printf("..\nmore than %s children exists, please use `obj` to query", maxN);
            }
            sys.out.println();
        }
        // 多个内容
        else {
            // 先输出所有的文件
            WnObjTable tab = new WnObjTable(keys);
            for (WnObj o : list) {
                if (o.isFILE()) {
                    tab.add(o, useColor, briefSize);
                }
            }
            sys.out.print(tab.toString());
            // 再输出所有的目录
            for (WnObj o : list) {
                if (!o.isFILE()) {
                    String rph = Disks.getRelativePath(p.path(), o.path());
                    final WnObjTable tabDir = new WnObjTable(keys);
                    sys.out.println(rph + " :");
                    boolean tooMany = __joint_children(sys,
                                                       useColor,
                                                       briefSize,
                                                       showHidden,
                                                       maxN,
                                                       tab,
                                                       o);
                    sys.out.print(tabDir.toString());
                    if (tooMany) {
                        sys.out.printf("... more than %s children exists ...", maxN);
                    }
                    sys.out.println();
                }
            }
        }

    }

    private boolean __joint_children(final WnSystem sys,
                                     final boolean useColor,
                                     final boolean briefSize,
                                     final boolean showHidden,
                                     final int maxN,
                                     final WnObjTable tab,
                                     WnObj o) {
        try {
            Wn.Io.eachChildren(sys.io, o, new Each<WnObj>() {
                public void invoke(int index, WnObj child, int length) {
                    // 超过了最多显示的个数
                    if (index >= maxN) {
                        throw Lang.makeThrow("ReachMax");
                    }
                    // 加入结果集
                    if (!child.isHidden() || showHidden)
                        tab.add(child, useColor, briefSize);
                }
            });
        }
        // 看看异常是怎么定义的
        catch (RuntimeException e) {
            // 明确退出，则表示子节点太多
            if (e.getMessage().equals("ReachMax"))
                return true;
            throw e;
        }
        return false;
    }

}
