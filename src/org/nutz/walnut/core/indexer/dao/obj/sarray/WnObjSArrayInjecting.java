package org.nutz.walnut.core.indexer.dao.obj.sarray;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.walnut.core.indexer.dao.obj.WnObjInjecting;

public class WnObjSArrayInjecting extends WnObjInjecting {

    private boolean asList;

    public WnObjSArrayInjecting(String stdName, boolean asList) {
        super(stdName);
        this.asList = asList;
    }

    @Override
    public void inject(Object obj, Object value) {
        // 预处理一下值
        if (null != value) {
            // 如果是字符串，需要尝试拆分一下数组
            if (value instanceof CharSequence) {
                String[] ss = Strings.splitIgnoreBlank(value.toString());
                if (this.asList) {
                    value = Lang.list(ss);
                } else {
                    value = ss;
                }
            }
        }
        // 注入
        super.inject(obj, value);
    }
}
