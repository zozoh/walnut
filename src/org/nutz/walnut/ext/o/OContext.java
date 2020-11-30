package org.nutz.walnut.ext.o;

import java.util.LinkedList;
import java.util.List;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmFilterContext;
import org.nutz.walnut.util.WnPager;

public class OContext extends JvmFilterContext {

    public List<WnObj> list;

    public WnPager pager;

    /**
     * 输出行为的过滤器，还是要设置一下这个值，这样主类就不输出了
     */
    public boolean alreadyOutputed;

    public OContext() {
        this.list = new LinkedList<>();
    }

    public void add(WnObj... objs) {
        for (WnObj o : objs) {
            list.add(o);
        }
    }

}
