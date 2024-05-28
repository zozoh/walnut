package com.site0.walnut.seq;

import java.util.Date;

public interface SeqGenerator {
    
    long next(Date hint);
    
}
