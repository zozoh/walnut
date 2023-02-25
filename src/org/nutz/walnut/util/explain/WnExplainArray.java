package org.nutz.walnut.util.explain;

import java.util.ArrayList;
import java.util.List;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.util.Wlang;
import org.nutz.walnut.util.each.WnEachIteratee;

public class WnExplainArray implements WnExplain {

    private boolean asArray;

    private int N;

    private List<WnExplain> list;

    public WnExplainArray(Object input) {
        this(input, false);
    }

    public WnExplainArray(Object input, boolean asArray) {
        this.asArray = asArray;
        this.N = Wlang.count(input);
        this.list = new ArrayList<>(N);

        Wlang.each(input, new WnEachIteratee<Object>() {
            public void invoke(int index, Object ele, Object src) {
                WnExplain exp = WnExplains.parse(ele);
                list.add(exp);
            }
        });
    }

    @Override
    public Object explain(NutBean context) {
        if (null == list) {
            return null;
        }
        List<Object> re = new ArrayList<>(N);
        for (WnExplain ex : list) {
            Object v = ex.explain(context);
            re.add(v);
        }
        if (asArray) {
            Object[] arr = new Object[N];
            re.toArray(arr);
            return arr;
        }
        return re;
    }

}
