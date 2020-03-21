package org.nutz.walnut.ext.bizhook;

import java.util.Map;

import org.nutz.lang.Strings;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.validate.NutValidate;

public class BizHookMatch {

    private String[] keys;

    /**
     * true: 根据 keys创建一个新对象，false:keys为fallback列表
     */
    private boolean isPick;

    private NutValidate validator;
    
    @SuppressWarnings("unchecked")
    public BizHookMatch(String key, Object val) {
        // 挑选模式
        if(key.indexOf(',')>=0) {
            this.keys = Strings.splitIgnoreBlank(key);
            this.isPick = true;
        }
        // fallback 模式
        else {
            this.keys = Strings.splitIgnoreBlank(key, "|");
            this.isPick = false;
        }
        // 建立检查器
        if(val instanceof Map<?,?>) {
            NutMap map = NutMap.WRAP((Map<String, Object>) val);
            this.validator = new NutValidate(map);
        }
        // 快捷模式
        else {
            this.validator = new NutValidate(val.toString());
        }
    }

    public boolean match(NutBean obj) {
        Object o2;
        // Pick Value
        if (isPick) {
            o2 = obj.pick(keys);
        }
        // Fallback value
        else {
            o2 = obj.getFallback(keys);
        }

        // 匹配
        return validator.match(o2);
    }
}
