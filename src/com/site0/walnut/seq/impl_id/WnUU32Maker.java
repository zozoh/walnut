package com.site0.walnut.seq.impl_id;

import java.util.Date;

import org.nutz.lang.random.R;

import com.site0.walnut.seq.IDMaker;

public class WnUU32Maker implements IDMaker {

    @Override
    public String make(Date hint) {
        return R.UU32();
    }

}
