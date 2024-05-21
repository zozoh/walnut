package com.site0.walnut.lookup;

import java.util.ArrayList;
import java.util.List;

import org.nutz.lang.util.NutBean;

import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Ws;

public class WnTestLookup implements WnLookup {

    private int mockCount;

    public WnTestLookup() {
        this(5);
    }

    public WnTestLookup(int mockCount) {
        this.mockCount = mockCount;
    }

    @Override
    public List<NutBean> lookup(String hint) {
        List<NutBean> list = new ArrayList<>(mockCount);
        for (int i = 0; i < mockCount; i++) {
            list.add(Wlang.map("text", Ws.repeat(hint, i + 1)).setv("value", hint + '_' + i));
        }
        return list;
    }

}
