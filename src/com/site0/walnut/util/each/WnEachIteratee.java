package com.site0.walnut.util.each;

public interface WnEachIteratee<T> {

    void invoke(int index, T ele, Object src) throws WnBreakException, WnContinueException;

}
