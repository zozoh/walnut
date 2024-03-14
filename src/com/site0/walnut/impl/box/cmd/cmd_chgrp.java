package com.site0.walnut.impl.box.cmd;

import org.nutz.lang.Each;
import org.nutz.lang.util.Disks;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;

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
            o.group(cc.str);
            sys.io.set(o, "^g$");
            if (cc.v) {
                String rpath = Disks.getRelativePath(cc.current.path(), o.path());
                sys.out.println(rpath);
            }
        }

        if (cc.R && !o.isFILE()) {
            sys.io.each(Wn.Q.pid(o.id()), new Each<WnObj>() {
                public void invoke(int index, WnObj child, int length) {
                    __do_ch(sys, cc, child);
                }
            });
        }
    }

}
