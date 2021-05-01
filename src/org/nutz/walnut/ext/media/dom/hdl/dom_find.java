package org.nutz.walnut.ext.media.dom.hdl;

import java.util.ArrayList;
import java.util.List;

import org.nutz.walnut.cheap.dom.CheapElement;
import org.nutz.walnut.cheap.dom.CheapMatcher;
import org.nutz.walnut.cheap.dom.match.CheapAutoMatcher;
import org.nutz.walnut.ext.media.dom.DomContext;
import org.nutz.walnut.ext.media.dom.DomFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class dom_find extends DomFilter {

    @Override
    protected void process(WnSystem sys, DomContext fc, ZParams params) {
        if (params.vals.length == 0)
            return;

        List<CheapMatcher> cms = new ArrayList<>(params.vals.length);

        for (String val : params.vals) {
            CheapMatcher cm = new CheapAutoMatcher(val);
            cms.add(cm);
        }

        List<CheapElement> list = fc.doc.findElements(el -> {
            for (CheapMatcher cm : cms) {
                if (cm.match(el)) {
                    return true;
                }
            }
            return false;
        });

        fc.selected.addAll(list);
    }

}
