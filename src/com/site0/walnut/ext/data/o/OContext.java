package com.site0.walnut.ext.data.o;

import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.impl.box.JvmFilterContext;
import com.site0.walnut.util.WnPager;
import com.site0.walnut.util.Wobj;
import com.site0.walnut.util.validate.WnMatch;

public class OContext extends JvmFilterContext {

    public NutMap vars;

    public List<WnObj> list;

    public WnPager pager;

    public boolean keepAsList;

    public String subKey;

    public NutMap summary;

    /**
     * 输出行为的过滤器，还是要设置一下这个值，这样主类就不输出了
     */
    public boolean quiet;

    public OContext() {
        this.vars = new NutMap();
        this.list = new LinkedList<>();
        this.summary = new NutMap();
    }

    public Object toOutput(boolean autoPath) {
        return toOutput(autoPath, new WnMatch[0]);
    }

    public Object toOutput(boolean autoPath, WnMatch... kms) {
        // 执行字段过滤
        List<? extends NutBean> outputs = Wobj.filterObjKeys(list, kms, subKey, autoPath);

        // 不要分页
        if (null == pager) {
            int len = outputs.size();
            // 保持列表
            if (keepAsList || len > 1) {
                return outputs;
            }
            // 自动拆包
            else if (len == 1) {
                return outputs.get(0);
            }
            // 返回 null
            return null;
        }

        // 输出翻页信息
        NutMap reo = new NutMap();
        reo.put("list", outputs);
        if (null != pager) {
            reo.put("pager", pager.toPagerObj(outputs.size()));
        }
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
