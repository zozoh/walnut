package com.site0.walnut.util;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;

/**
 * 专门给 `Wn.explainObj` 设计的 `=>` 动态函数支持
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnEvalHelper {

    private static WnEvalHelper _me = new WnEvalHelper();

    public static WnEvalHelper me() {
        return _me;
    }

    public String toJson(Object any, boolean compact) {
        JsonFormat jfmt = JsonFormat.nice();
        jfmt.setQuoteName(true).setCompact(compact);
        return Json.toJson(any, jfmt);
    }

    public String toJsonOnly(Object any, String actived) {
        JsonFormat jfmt = JsonFormat.nice();
        jfmt.setQuoteName(true).setCompact(true);
        jfmt.setActived(actived);
        return Json.toJson(any, jfmt);
    }

    public Object fromJson(String input) {
        return Json.fromJson(input);
    }

}
