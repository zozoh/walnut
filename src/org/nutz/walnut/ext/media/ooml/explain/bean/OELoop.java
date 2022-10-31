package org.nutz.walnut.ext.media.ooml.explain.bean;

import java.util.List;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.cheap.dom.CheapElement;

public class OELoop extends OENode {

    private String listName;

    public OELoop() {
        this.type = OENodeType.LOOP;
    }

    @Override
    public CheapElement renderTo(CheapElement pEl, NutBean vars) {
        Object list = vars.get(listName);
        // 防空
        if (null == list || !hasChildren()) {
            return pEl;
        }

        if (list instanceof List<?>) {
            Object old = vars.get(varName);
            for (Object li : (List<?>) list) {
                vars.put(varName, li);
                for (OEItem it : children) {
                    it.renderTo(pEl, vars);
                }
            }
            vars.put(varName, old);
        }

        return pEl;
    }

    public String getListName() {
        return listName;
    }

    public void setListName(String listName) {
        this.listName = listName;
    }

}
