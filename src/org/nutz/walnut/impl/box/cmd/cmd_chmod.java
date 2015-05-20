package org.nutz.walnut.impl.box.cmd;

import org.nutz.lang.Each;
import org.nutz.lang.Lang;
import org.nutz.lang.util.Disks;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public class cmd_chmod extends cmd_chxxx {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {

        _ch_context cc = _eval_params(sys, args);

        // 得到 Mod
        String sMod = cc.str;

        // 看看是那种形式
        // TODO zzh : 这里还是先实现一个凑合版，之后再实现完整版
        int op;
        int md;
        // 八进制
        if (sMod.matches("^[0-7]{3}")) {
            op = 0;
            md = Integer.parseInt(sMod, 8);
        }
        // +rwx
        else if (sMod.matches("^[+-=][rwx]{1,}$")) {
            op = 0;
            String perm = sMod.substring(1);
            int bits = 0;
            if (perm.indexOf('r') != -1)
                bits |= Wn.Io.R;
            if (perm.indexOf('w') != -1)
                bits |= Wn.Io.W;
            if (perm.indexOf('x') != -1)
                bits |= Wn.Io.X;

            md = 0;
            md |= bits;
            md |= bits << 3;
            md |= bits << 6;

            if (sMod.startsWith("+")) {
                op = 1;
            } else if (sMod.startsWith("-")) {
                op = -1;
            } else if (sMod.startsWith("=")) {
                op = 0;
            } else {
                throw Lang.impossible();
            }
        }
        // 抛错吧
        else {
            throw Er.create("e.cmd.chmod.invalid", sMod);
        }

        // 输出内容
        for (WnObj o : cc.list) {
            __do_ch(sys, cc, op, md, o);
        }
    }

    private void __do_ch(final WnSystem sys,
                         final _ch_context cc,
                         final int op,
                         final int md,
                         WnObj o) {
        int oldMd = o.mode();

        int newMd = oldMd;
        switch (op) {
        case -1:
            newMd = oldMd & (~md);
            break;
        case 0:
            newMd = md;
            break;
        case 1:
            newMd = oldMd | md;
            break;
        default:
            throw Lang.impossible();
        }

        if (newMd != oldMd) {
            sys.io.appendMeta(o, "md:" + newMd);
            if (cc.v) {
                String rpath = Disks.getRelativePath(cc.current.path(), o.path());
                sys.out.println(rpath);
            }
        }

        if (cc.R && !o.isFILE()) {
            sys.io.eachChildren(o, null, new Each<WnObj>() {
                public void invoke(int index, WnObj child, int length) {
                    __do_ch(sys, cc, op, md, child);
                }
            });
        }
    }
}
