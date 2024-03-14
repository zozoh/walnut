package com.site0.walnut.ext.util.jsonx.util;

import java.util.ArrayList;
import java.util.List;

import org.nutz.json.Json;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.ZParams;
import com.site0.walnut.util.validate.WnMatch;
import com.site0.walnut.util.validate.impl.AutoMatch;

/**
 * <h1>它过滤</h1>
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class JsonXFilterGivenMatch implements WnMatch {

    private boolean isAnd;

    private List<WnMatch> wms;

    public JsonXFilterGivenMatch(WnSystem sys, ZParams params) {
        this.isAnd = params.is("and");

        // 看看参数
        boolean isFile = params.is("f");
        boolean isMeta = params.is("meta");

        // 准备过滤条件
        this.wms = new ArrayList<>(params.vals.length);

        // 循环加入条件
        for (String val : params.vals) {
            WnMatch wm;
            // 文件元数据
            if (isMeta) {
                WnObj o = Wn.checkObj(sys, val);
                wm = new AutoMatch(o);
            }
            // 文件内容
            else if (isFile) {
                WnObj o = Wn.checkObj(sys, val);
                String input = sys.io.readText(o);
                Object obj = Json.fromJson(input);
                wm = new AutoMatch(obj);
            }
            // 直接就是 JSON
            else {
                Object obj = Json.fromJson(val);
                wm = new AutoMatch(obj);
            }

            // 记入
            wms.add(wm);
        }
    }

    @Override
    public boolean match(Object input) {
        return JsonXFilters.match(input, wms, isAnd);
    }

}
