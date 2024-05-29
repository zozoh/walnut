package com.site0.walnut.val.id;

import java.util.Date;

import org.nutz.lang.random.R;
import org.nutz.lang.util.NutBean;

import com.site0.walnut.val.ValueMaker;

public class WnUU32Maker implements ValueMaker {

    @Override
    public String make(Date hint, NutBean context) {
        return R.UU32();
    }

}
