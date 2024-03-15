package org.nutz.ioc.val;

import java.util.Collection;

import org.nutz.ioc.IocMaking;
import org.nutz.ioc.ValueProxy;
import com.site0.walnut.util.Wlang;

public abstract class ListableValueProxy implements ValueProxy {

    protected Object obj;

    public ListableValueProxy(Object obj) {
        this.obj = obj;
    }

    protected abstract Object getValue(String key);

    public Object get(IocMaking ing) {
        if (obj == null)
            return null;
        if (obj.getClass().isArray() || obj instanceof Collection) {} else {
            obj = new Object[]{obj};
        }
        final StringBuilder sb = new StringBuilder();
        Wlang.each(obj, (int index, Object ele, Object src) -> {
            String key = String.valueOf(ele);
            if (key.startsWith("!")) {
                key = key.substring(1);
                String dft = "";
                if (key.contains(":")) {
                    dft = key.substring(key.indexOf(':') + 1);
                    key = key.substring(0, key.indexOf(':'));
                }
                Object val = getValue(key);
                if (val != null) {
                    sb.append(val);
                } else {
                    sb.append(dft);
                }
                return;
            }
            Object val = getValue(key);
            if (val == null) {
                sb.append(key);
            } else {
                sb.append(val);
            }
        });
        return sb.toString();
    }
}
