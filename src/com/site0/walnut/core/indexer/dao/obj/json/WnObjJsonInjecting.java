package com.site0.walnut.core.indexer.dao.obj.json;

import org.nutz.json.Json;
import org.nutz.log.Log;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.core.indexer.dao.obj.WnObjInjecting;

public class WnObjJsonInjecting extends WnObjInjecting {

    private static final Log log = Wlog.getIO();

    public WnObjJsonInjecting(String stdName) {
        super(stdName);
    }

    @Override
    public void inject(Object obj, Object value) {
        // 预处理一下值
        if (null != value) {
            // 如果是字符串，需要尝试解析 JSON
            if (value instanceof CharSequence) {
                try {
                    value = Json.fromJson(value.toString());
                }
                catch (Exception e) {
                    if (log.isWarnEnabled()) {
                        log.warn("Invalid JSON field format: " + value, e);
                    }
                }
            }
        }
        // 注入
        super.inject(obj, value);
    }

}
