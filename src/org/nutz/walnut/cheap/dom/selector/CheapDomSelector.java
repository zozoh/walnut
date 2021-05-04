package org.nutz.walnut.cheap.dom.selector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.nutz.walnut.cheap.dom.CheapElement;
import org.nutz.walnut.util.Ws;

public class CheapDomSelector implements CheapSelector {

    private List<CheapSelector> selectors;

    public CheapDomSelector(String input) {
        this.valueOf(input);
    }

    @Override
    public int join(List<CheapElement> list, CheapElement el, int limit) {
        int re = 0;
        for (CheapSelector sel : selectors) {
            re += sel.join(list, el, limit - re);
        }
        return re;
    }

    @Override
    public boolean match(CheapElement el) {
        for (CheapSelector sel : selectors) {
            if (sel.match(el)) {
                return true;
            }
        }
        return false;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        joinString(sb);
        return sb.toString();
    }

    @Override
    public void joinString(StringBuilder sb) {
        Iterator<CheapSelector> it = selectors.iterator();
        if (it.hasNext()) {
            CheapSelector sel = it.next();
            sb.append(sel);
        }
        while (it.hasNext()) {
            CheapSelector sel = it.next();
            sb.append(", ");
            sb.append(sel);
        }
    }

    @Override
    public CheapSelector valueOf(String input) {
        String[] ss = Ws.splitIgnoreBlank(input);
        selectors = new ArrayList<>(ss.length);
        for (String s : ss) {
            CheapAxisSelector sel = new CheapAxisSelector(s);
            selectors.add(sel);
        }
        return this;
    }

}
