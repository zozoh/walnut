package com.site0.walnut.impl.box.cmd;

import org.nutz.lang.Each;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Disks;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.login.WnUser;
import com.site0.walnut.util.Wn;

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
            throw Wlang.impossible();
        }

        // 确保有这个用户
        WnUser u = sys.auth.checkUser(unm);

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
