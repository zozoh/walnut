package org.nutz.walnut.core.indexer.dao.obj.json;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.walnut.core.indexer.dao.obj.WnObjEjecting;

public class WnObjJsonEjecting extends WnObjEjecting {

    private JsonFormat jfmt;

    public WnObjJsonEjecting(String stdName) {
        super(stdName);
        this.jfmt = JsonFormat.compact().setQuoteName(true);
    }

    @Override
    public Object eject(Object obj) {
        Object val = super.eject(obj);
        // 如果不是字符串，应该格式化成 JSON
        if (null != val) {
            if (val instanceof CharSequence) {
                return val;
            } else {
                return Json.toJson(val, jfmt);
            }
        }
        // 嗯，直接返回吧
        return val;
    }

}
