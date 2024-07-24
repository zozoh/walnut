package com.site0.walnut.ext.data.val.hdl;

import java.util.Date;

import com.site0.walnut.ext.data.val.ValContext;
import com.site0.walnut.ext.data.val.ValFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;
import com.site0.walnut.val.ValueMaker;
import com.site0.walnut.val.ValueMakers;

public class val_gen extends ValFilter {

    @Override
    protected void process(WnSystem sys, ValContext fc, ZParams params) {
        String input = params.val_check(0);
        // 获取值的生成器
        ValueMaker vmk = ValueMakers.build(sys, input);

        // 生成值
        Date hint = new Date();
        fc.result = vmk.make(hint, fc.context);

    }

}
