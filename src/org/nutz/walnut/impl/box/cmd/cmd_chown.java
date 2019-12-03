package org.nutz.walnut.impl.box.cmd;

import org.nutz.lang.Each;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Disks;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public class cmd_chown extends cmd_chxxx {

    @Override
    public void exec(WnSystem sys, String[] args) {

        _ch_context cc = _eval_params(sys, args);

        // 得到用户和组
        String unm, grp;
        String[] ss = Strings.splitIgnoreBlank(cc.str, ":");
        if (ss.length == 2) {
            unm = ss[0];
            grp = ss[1];
        } else if (ss.length == 1) {
            unm = ss[0];
            grp = null;
        } else {
            throw Lang.impossible();
        }

        // 确保有这个用户
        WnAccount u = sys.auth.checkAccount(unm);

        // 输出内容
        for (WnObj o : cc.list) {
            __do_ch(sys, cc, u.getName(), grp, o);
        }
    }

    private void __do_ch(final WnSystem sys,
                         final _ch_context cc,
                         final String unm,
                         final String grp,
                         WnObj o) {
        NutMap map = new NutMap();
        if (null != grp && !o.group().equals(grp)) {
            map.setv("g", grp);
        }
        if (null != unm && !o.creator().equals(unm)) {
            map.setv("c", unm);
        }

        if (map.size() > 0) {
            sys.io.appendMeta(o, map);
            if (cc.v) {
                String rpath = Disks.getRelativePath(cc.current.path(), o.path());
                sys.out.println(rpath);
            }
        }

        if (cc.R && !o.isFILE()) {
            sys.io.each(Wn.Q.pid(o.id()), new Each<WnObj>() {
                public void invoke(int index, WnObj child, int length) {
                    __do_ch(sys, cc, unm, grp, child);
                }
            });
        }
    }

}
