package com.site0.walnut.ext.data.o.impl.pop;

import java.util.LinkedList;
import java.util.List;

import com.site0.walnut.ext.data.o.util.WnPop;

public class PopAll implements WnPop {

    @Override
    public <T extends Object> List<T> exec(List<T> list) {
        return new LinkedList<>();
    }

}
