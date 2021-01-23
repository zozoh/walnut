package org.nutz.walnut.ext.o;

import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmFilterContext;
import org.nutz.walnut.util.WnPager;
import org.nutz.walnut.util.Wobj;
import org.nutz.walnut.validate.WnMatch;

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
        return toOutput(null);
    }

    public Object toOutput(WnMatch km) {
        // 执行字段过滤
        List<? extends NutBean> outputs = Wobj.filterObjKeys(list, km);

        // 自动拆包列表
        if (null == pager || !keepAsList) {
            if (outputs.size() == 0) {
                return null;
            }
            if (outputs.size() == 1) {
                return list.get(0);
            }
            return outputs;
        }

        // 翻页了，那么一定要输出列表啊
        NutMap reo = new NutMap();
        reo.put("list", outputs);
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
