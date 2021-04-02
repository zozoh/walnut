package org.nutz.walnut.cheap.dom.match;

import java.util.LinkedList;
import java.util.List;

import org.nutz.walnut.cheap.dom.CheapElement;
import org.nutz.walnut.cheap.dom.CheapMatcher;

public class CheapOrMatcher implements CheapMatcher {

    private List<CheapMatcher> list;

    public CheapOrMatcher() {
        list = new LinkedList<>();
    }

    public void addMatcher(CheapMatcher cm) {
        list.add(cm);
    }

    @Override
    public boolean match(CheapElement el) {
        for (CheapMatcher cm : list) {
            if (cm.match(el))
                return true;
        }
        return false;
    }

}
