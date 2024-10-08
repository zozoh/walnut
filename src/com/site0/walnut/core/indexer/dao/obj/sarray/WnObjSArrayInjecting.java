package com.site0.walnut.core.indexer.dao.obj.sarray;

import com.site0.walnut.util.Wlang;
import org.nutz.lang.Strings;
import com.site0.walnut.core.indexer.dao.obj.WnObjInjecting;

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
                    value = Wlang.list(ss);
                } else {
                    value = ss;
                }
            }
        }
        // 注入
        super.inject(obj, value);
    }
}
