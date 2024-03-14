package com.site0.walnut.ext.util.jsonx;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import org.nutz.lang.util.NutMap;
import com.site0.walnut.impl.box.JvmFilterContext;
import com.site0.walnut.util.Ws;

public class JsonXContext extends JvmFilterContext {

    public Object obj;

    public boolean quite;

    @SuppressWarnings("unchecked")
    public LinkedList<Object> checkList(String toKey, boolean asPath) {
        // 准备返回列表
        LinkedList<Object> col;

        if (!Ws.isBlank(toKey)) {
            NutMap map = NutMap.WRAP((Map<String, Object>) this.obj);
            // 键路径
            if (asPath) {
                String[] ss = Ws.splitIgnoreBlank(toKey, "[.]");
                if (ss.length > 1) {
                    for (int i = 0; i < ss.length - 1; i++) {
                        String k = ss[i];
                        NutMap m2 = map.getAs(k, NutMap.class);
                        if (null == m2) {
                            m2 = new NutMap();
                            map.put(k, m2);
                        }
                        map = m2;
                    }
                    toKey = ss[ss.length - 1];
                }
            }
            // 直接找到对象
            Object it = map.get(toKey);
            if (null == it) {
                col = new LinkedList<Object>();
            }
            // 本身就是列表
            else if (it instanceof Collection<?>) {
                col = new LinkedList<Object>();
                col.addAll((Collection<Object>) it);
            }
            // 本身需要变成列表
            else {
                col = new LinkedList<Object>();
                col.add(it);
            }
            // 确保设置一下列表
            map.put(toKey, col);
        }
        // 就操作顶层上下文
        else {
            if (null == this.obj) {
                col = new LinkedList<Object>();
            }
            // 本身就是列表
            else if (this.obj instanceof Collection<?>) {
                col = new LinkedList<Object>();
                col.addAll((Collection<Object>) this.obj);
            }
            // 本身需要变成列表
            else {
                col = new LinkedList<Object>();
                col.add(this.obj);
            }
            this.obj = col;
        }

        return col;
    }

}
