package com.site0.walnut.val;

import java.util.Date;

import org.nutz.lang.util.NutBean;

public interface SeqMaker {
    
    long make(Date hint, NutBean context);
    
}
