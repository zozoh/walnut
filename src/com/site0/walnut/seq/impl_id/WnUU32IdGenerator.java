package com.site0.walnut.seq.impl_id;

import java.util.Date;

import org.nutz.lang.random.R;

import com.site0.walnut.seq.WnIdGenerator;

public class WnUU32IdGenerator implements WnIdGenerator {

    @Override
    public String next(Date hint) {
        return R.UU32();
    }

}
