package com.site0.walnut.ext.data.val.hdl;

import com.site0.walnut.ext.data.val.ValContext;
import com.site0.walnut.ext.data.val.ValFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.ZParams;

public class val_macro extends ValFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "n");
    }

    @Override
    protected void process(WnSystem sys, ValContext fc, ZParams params) {
        String str = params.val_check(0);
        Object out = Wn.fmt_str_macro(str);
        if (params.is("n")) {
            sys.out.print(out.toString());
        } else {
            sys.out.println(out.toString());
        }
    }

}
