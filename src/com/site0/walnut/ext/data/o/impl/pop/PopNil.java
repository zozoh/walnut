package com.site0.walnut.ext.data.o.impl.pop;

import java.util.ArrayList;
import java.util.List;

import com.site0.walnut.ext.data.o.util.WnPop;

public class PopNil implements WnPop {

    @Override
    public <T extends Object> List<T> exec(List<T> list) {
        List<T> re = new ArrayList<>(list.size());
        for (T li : list) {
            if (null != li) {
                re.add(li);
            }
        }
        return re;
    }

}
