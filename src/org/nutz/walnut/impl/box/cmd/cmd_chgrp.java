package org.nutz.walnut.impl.box.cmd;

import org.nutz.lang.Each;
import org.nutz.lang.util.Disks;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.WnSystem;

public class cmd_chgrp extends cmd_chxxx {

    @Override
    public void exec(WnSystem sys, String[] args) {

        _ch_context cc = _eval_params(sys, args);

        // 输出内容
        for (WnObj o : cc.list) {
            __do_ch(sys, cc, o);
        }
    }

    private void __do_ch(final WnSystem sys, final _ch_context cc, WnObj o) {
        if (!o.group().equals(cc.str)) {
            sys.io.appendMeta(o, "g:" + cc.str);
            if (cc.v) {
                String rpath = Disks.getRelativePath(cc.current.path(), o.path());
                sys.out.println(rpath);
            }
        }

        if (cc.R && !o.isFILE()) {
            sys.io.eachChildren(o, null, new Each<WnObj>() {
                public void invoke(int index, WnObj child, int length) {
                    __do_ch(sys, cc, child);
                }
            });
        }
    }

}
