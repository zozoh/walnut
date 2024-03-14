package com.site0.walnut.ext.data.thing.impl;

import java.util.List;

import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.ext.data.thing.ThingDataAction;

public class FileQueryAction extends ThingDataAction<List<WnObj>> {

    public NutMap sort;

    @Override
    public List<WnObj> invoke() {
        WnQuery q = _Q();
        if (null != sort && sort.size() > 0) {
            q.sort(sort);
        }
        // 默认按名称排序
        else {
            q.asc("nm");
        }

        // 返回结果
        return io.query(q);
    }

}
