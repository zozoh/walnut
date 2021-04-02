package org.nutz.walnut.cheap.dom.match;

import java.util.LinkedList;
import java.util.List;

import org.nutz.walnut.cheap.dom.CheapElement;
import org.nutz.walnut.cheap.dom.CheapMatcher;

public class CheapAndMatcher implements CheapMatcher {

    private List<CheapMatcher> list;

    public CheapAndMatcher() {
        list = new LinkedList<>();
    }

    public void addMatcher(CheapMatcher cm) {
        list.add(cm);
    }

    @Override
    public boolean match(CheapElement el) {
        for (CheapMatcher cm : list) {
            if (!cm.match(el))
                return false;
        }
        return true;
    }

}
