package com.site0.walnut.lookup;

import java.util.ArrayList;
import java.util.List;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Ws;

public class WnTestLookup implements WnLookup {

    private int mockCount;

    private int maxStrLen;

    public WnTestLookup() {
        this(5, 30);
    }

    public WnTestLookup(int mockCount, int maxStrLen) {
        this.mockCount = mockCount;
        this.maxStrLen = maxStrLen;
    }

    @Override
    public List<NutBean> lookup(String hint) {
        List<NutBean> list = new ArrayList<>(mockCount);
        for (int i = 0; i < mockCount; i++) {
            String text = Ws.repeat(hint, i + 1);
            if (text.length() > maxStrLen) {
                text = text.substring(0, maxStrLen) + "...";
            }
            NutMap bean = Wlang.map("text", text);
            bean.setv("value", hint + '_' + i);
            list.add(bean);
        }
        return list;
    }

}
