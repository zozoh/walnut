package com.site0.walnut.val;

import java.util.Date;

import org.nutz.lang.util.NutBean;

public interface ValueMaker {

    Object make(Date hint, NutBean context);

}
