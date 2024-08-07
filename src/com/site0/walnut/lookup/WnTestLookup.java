package com.site0.walnut.lookup;

import java.util.ArrayList;
import java.util.List;

import org.nutz.lang.random.R;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Ws;

public class WnTestLookup implements WnLookup {

    private int maxStrLen;

    public WnTestLookup() {
        this(30);
    }

    public WnTestLookup(int maxStrLen) {
        this.maxStrLen = maxStrLen;
    }

    @Override
    public List<NutBean> lookup(String hint, int limit) {
        List<NutBean> list = new ArrayList<>(limit);
        for (int i = 0; i < limit; i++) {
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

    @Override
    public List<NutBean> fetch(String id) {
        NutMap bean = Wlang.map("text", "Fake-" + R.sg(4));
        bean.setv("value", id);
        return null;
    }

}
