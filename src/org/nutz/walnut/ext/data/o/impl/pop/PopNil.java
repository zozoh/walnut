package org.nutz.walnut.ext.data.o.impl.pop;

import java.util.ArrayList;
import java.util.List;

import org.nutz.walnut.ext.data.o.util.WnPop;

public class PopNil implements WnPop {

    @Override
    public List<Object> pop(List<Object> list) {
        List<Object> re = new ArrayList<Object>(list.size());
        for (Object li : list) {
            if (null != li) {
                re.add(li);
            }
        }
        return re;
    }

}
