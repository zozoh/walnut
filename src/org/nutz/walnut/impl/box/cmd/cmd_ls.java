package org.nutz.walnut.impl.box.cmd;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.Each;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.Times;
import org.nutz.lang.util.Disks;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.LinuxTerminal;
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
        WnObj p = evalCandidateObjs(sys, params.vals, list, true);

        // 检查是否候选对象列表为空
        checkCandidateObjsNoEmpty(args, list);

        // 只有一个内容
        if (list.size() == 1) {
            WnObj o = list.get(0);
            // 本身就是文件
            if (o.isFILE()) {
                __print(o, sys, params);
            }
            // 是个目录
            else {
                sys.io.eachChildren(o, null, new Each<WnObj>() {
                    public void invoke(int index, WnObj child, int length) {
                        __print(child, sys, params);
                    }
                });
            }
        }
        // 多个内容
        else {
            // 先输出所有的文件
            for (WnObj o : list) {
                if (o.isFILE()) {
                    __print(o, sys, params);
                }
            }
            sys.out.writeLine();
            // 再输出所有的目录
            for (WnObj o : list) {
                if (!o.isFILE()) {
                    String rph = Disks.getRelativePath(p.path(), o.path());
                    sys.out.writeLine(rph + " :");
                    sys.io.eachChildren(o, null, new Each<WnObj>() {
                        public void invoke(int index, WnObj obj, int length) {
                            __print(obj, sys, params);
                        }
                    });
                }
            }
        }
    }

    private void __print(WnObj o, WnSystem sys, ZParams params) {
        if (params.is("A") || !o.isHidden()) {
            // 输出完整信息
            if (params.is("l")) {
                StringBuilder sb = new StringBuilder();
                // ID
                if (params.is("i")) {
                    sb.append(o.id()).append(' ');
                }
                // RACE
                switch (o.race()) {
                case DIR:
                    sb.append('d');
                    break;
                case FILE:
                    sb.append('-');
                    break;
                case OBJ:
                    sb.append('o');
                    break;
                default:
                    throw Lang.impossible();
                }

                // Mode
                sb.append(Wn.Io.modeToStr(o.mode())).append(' ');

                // Mender
                sb.append(Strings.alignLeft(o.creator(), 8, ' ')).append(' ');
                // GRP
                sb.append(Strings.alignLeft(o.group(), 8, ' ')).append(' ');
                // len
                if (params.is("h")) {
                    String sLen = __show_size(o.len(), 1000, 0, 'B', 'K', 'M', 'G', 'T');
                    sb.append(Strings.alignRight(sLen, 8, ' ')).append(' ');
                } else {
                    sb.append(Strings.alignRight(o.len(), 8, ' ')).append(' ');
                }
                // lm
                Date lm = new Date(o.lastModified());
                sb.append(Strings.alignRight(Times.format("M", lm), 2, ' '));
                sb.append(' ');
                sb.append(Strings.alignRight(Times.format("d", lm), 2, ' '));
                sb.append(' ');
                sb.append(Times.format("HH:mm", lm)).append(' ');

                // name
                String nm = __get_display_name(o, sys);
                sb.append(nm);

                // 输出
                sys.out.writeLine(sb);
            }
            // 简单模式
            else {
                String nm = __get_display_name(o, sys);
                sys.out.writeLine(nm);
            }
        }
    }

    private String __get_display_name(WnObj o, WnSystem sys) {
        String nm = o.name();
        // 没有管道输出，则考虑输出颜色
        int font = -1;
        int color = -1;
        if (sys.nextId < 0) {
            // 目录
            if (o.isDIR()) {
                font = o.isHidden() ? 2 : 1;
                color = 34;
            }
            // 对象
            else if (o.isOBJ()) {
                font = 3;
                color = 33;
            }
            // 文件
            else if (o.isFILE()) {
                if (o.isHidden()) {
                    font = 2;
                }
            }
            // 不可能
            else {
                throw Lang.impossible();
            }
        }
        // 输出颜色
        if (font != -1 || color != -1) {
            return LinuxTerminal.wrapFont(nm, font, color);
        }
        return nm;
    }

    private String __show_size(long sz, long d, int uoff, char... unit) {
        if (sz < d || uoff >= unit.length - 1)
            return sz + "" + unit[uoff];
        return __show_size(sz / d, d, uoff, unit);
    }

}
