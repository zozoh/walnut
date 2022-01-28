package org.nutz.walnut.ext.data.o.impl.pop;

import java.util.LinkedList;
import java.util.List;

import org.nutz.walnut.ext.data.o.util.WnPop;

public class PopAll implements WnPop {

    @Override
    public <T extends Object> List<T> exec(List<T> list) {
        return new LinkedList<>();
    }

}
