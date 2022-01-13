package org.nutz.walnut.ext.util.jsonx.util;

import java.util.LinkedList;
import java.util.List;

import org.nutz.walnut.util.each.WnEachIteratee;
import org.nutz.walnut.util.validate.WnMatch;

public class JsonXFilterIteratee implements WnEachIteratee<Object> {

    private List<Object> list;

    private WnMatch wm;

    private boolean not;

    public JsonXFilterIteratee(WnMatch wm, boolean not) {
        this.list = new LinkedList<>();
        this.wm = wm;
        this.not = not;
    }

    @Override
    public void invoke(int index, Object ele, Object src) {
        if (wm.match(ele) ^ not) {
            list.add(ele);
        }
    }

    public List<Object> getResult() {
        return this.list;
    }

}
