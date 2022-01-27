package org.nutz.walnut.ext.data.o.impl.pop;

import java.util.ArrayList;
import java.util.List;

import org.nutz.walnut.ext.data.o.util.WnPop;

public abstract class PopMatch implements WnPop {

    private boolean not;

    @Override
    public List<Object> pop(List<Object> list) {
        List<Object> re = new ArrayList<Object>(list.size());
        for (Object li : list) {
            if (!(this.isMatch(li) ^ not)) {
                re.add(li);
            }
        }
        return re;
    }

    public boolean isNot() {
        return not;
    }

    public void setNot(boolean not) {
        this.not = not;
    }

    abstract protected boolean isMatch(Object ele);

}
