package org.nutz.walnut.ext.o;

import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmFilterContext;
import org.nutz.walnut.util.WnPager;

public class OContext extends JvmFilterContext {

    public List<WnObj> list;

    public WnPager pager;

    public boolean keepAsList;

    /**
     * 输出行为的过滤器，还是要设置一下这个值，这样主类就不输出了
     */
    public boolean alreadyOutputed;

    public OContext() {
        this.list = new LinkedList<>();
    }

    public Object toOutput() {
        if (null == pager || !keepAsList) {
            if (list.size() == 0) {
                return null;
            }
            if (list.size() == 1) {
                return list.get(0);
            }
            return list;
        }

        // 翻页了，那么一定要输出列表啊
        NutMap reo = new NutMap();
        reo.put("list", list);
        reo.put("pager", pager);
        return reo;
    }

    public void add(WnObj... objs) {
        for (WnObj o : objs) {
            list.add(o);
        }
    }

    public void clearAll() {
        this.list.clear();
        this.pager = null;
    }

}
