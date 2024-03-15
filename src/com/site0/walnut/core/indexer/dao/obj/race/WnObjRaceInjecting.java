package com.site0.walnut.core.indexer.dao.obj.race;

import com.site0.walnut.util.Wlang;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.core.indexer.dao.obj.WnObjInjecting;

public class WnObjRaceInjecting extends WnObjInjecting {

    public WnObjRaceInjecting(String stdName) {
        super(stdName);
    }

    @Override
    public void inject(Object obj, Object value) {
        // 默认当作文件
        if (null == value || value == WnRace.FILE || "1".equals(value)) {
            value = "FILE";
        }
        // 目录
        else if ("0".equals(value) || value == WnRace.DIR) {
            value = "DIR";
        }
        // 转换数字
        else if (value instanceof Number) {
            int v = ((Number) value).intValue();
            // 目录: 0
            if (0 == v) {
                value = "DIR";
            }
            // 文件: 1
            else if (1 == v) {
                value = "FILE";
            }
            // 其他的，抛错
            else {
                throw Wlang.impossible();
            }
        }
        // 注入
        super.inject(obj, value);
    }

}
