package org.nutz.walnut.cheap.dom.match;

import java.util.LinkedList;
import java.util.List;

import org.nutz.walnut.cheap.dom.CheapElement;
import org.nutz.walnut.cheap.dom.CheapMatcher;

public class CheapAndMatcher implements CheapMatcher {

    List<CheapMatcher> list;

    public CheapAndMatcher() {
        list = new LinkedList<>();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        joinString(sb);
        return sb.toString();
    }

    void joinString(StringBuilder sb) {
        for (CheapMatcher cm : list) {
            sb.append(cm.toString());
        }
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
