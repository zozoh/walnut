package org.nutz.walnut.core.indexer.dao.obj.sarray;

import java.util.Collection;

import org.nutz.lang.Strings;
import org.nutz.walnut.core.indexer.dao.obj.WnObjEjecting;

public class WnObjSArrayEjecting extends WnObjEjecting {

    public WnObjSArrayEjecting(String stdName) {
        super(stdName);
    }

    @Override
    public Object eject(Object obj) {
        Object val = super.eject(obj);
        // 如果不是字符串，应该格式化成 半角逗分隔的字符串
        if (null != val) {
            if (val instanceof CharSequence) {
                return val;
            }
            // 数组
            if (val.getClass().isArray()) {
                return Strings.join(",", (Object[]) val);
            }
            // 集合
            if (val instanceof Collection<?>) {
                return Strings.join(",", (Collection<?>) val);
            }
            // 普通对象，转字符串
            return val.toString();
        }
        // 嗯，直接返回吧
        return val;
    }
}
