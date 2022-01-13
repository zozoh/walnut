package org.nutz.walnut.ext.util.jsonx.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wlang;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.ZParams;
import org.nutz.walnut.util.validate.WnMatch;
import org.nutz.walnut.util.validate.impl.AutoMatch;

/**
 * <h1>自过滤</h1>
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class JsonXFilterSelfMatch implements WnMatch {

    private boolean isAnd;

    private List<EleKey> keys;

    private Object context;

    public JsonXFilterSelfMatch(WnSystem sys, ZParams params) {
        this.isAnd = params.is("and");

        // 读取上下文
        boolean isFile = params.is("f");
        boolean isMeta = params.is("meta");
        String self = params.getString("self");

        // 文件元数据
        if (isMeta) {
            context = Wn.checkObj(sys, self);
        }
        // 文件内容
        else if (isFile) {
            WnObj o = Wn.checkObj(sys, self);
            String input = sys.io.readText(o);
            context = Json.fromJson(input);
        }
        // 直接就是 JSON
        else {
            context = Wlang.map(self);
        }

        // 准备元数据的键
        this.keys = new ArrayList<>(params.vals.length);
        for (String val : params.vals) {
            EleKey key = new EleKey(val);
            keys.add(key);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean match(Object ele) {
        // 检查元素是否符合条件
        if (null == ele || !(ele instanceof Map)) {
            return false;
        }
        // 从元素里取值作为条件
        NutMap map = NutMap.WRAP((Map<String, Object>) ele);
        List<WnMatch> wms = new ArrayList<>(keys.size());

        // 循环取值
        for (EleKey key : keys) {
            Object vo = key.prepareObj(map);
            WnMatch wm = new AutoMatch(vo);
            wms.add(wm);
        }

        // 执行判断
        return JsonXFilters.match(context, wms, isAnd);
    }

    /**
     * 如何从元素中获取条件的抽象键
     */
    static class EleKey {

        String key;

        /**
         * 压值键，需要声明这个字段
         */
        String targetKey;

        EleKey(String str) {
            String[] ss = Ws.splitIgnoreBlank(str, "[:,;/]");
            if (ss.length == 1) {
                this.key = ss[0];
            } else if (ss.length > 1) {
                this.key = ss[0];
                this.targetKey = ss[1];
            }
            // TODO 或许可以支持更多，将多个元素键值压入一个返回 Map
        }

        Object prepareObj(NutMap ele) {
            Object val = ele.get(key);
            if (null == targetKey) {
                return val;
            }
            NutMap re = new NutMap();
            re.put(targetKey, val);
            return re;
        }
    }

}
