package org.nutz.walnut.validate;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.nutz.castor.Castors;
import org.nutz.lang.Mirror;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.validate.impl.*;

public class WnValidate {

    private static final Map<String, WnValidator> VALIDATORS = new HashMap<>();

    static {
        VALIDATORS.put("notNil", new NotNil());
        VALIDATORS.put("notEmpty", new NotEmpty());
        VALIDATORS.put("notBlank", new NotBlank());
        VALIDATORS.put("isNil", new IsNil());
        VALIDATORS.put("isEmpty", new IsEmpty());
        VALIDATORS.put("isBlank", new IsBlank());
        VALIDATORS.put("isPlainObject", new IsPlainObject());
        VALIDATORS.put("isBoolean", new IsBoolean());
        VALIDATORS.put("isTrue", new IsTrue());
        VALIDATORS.put("isFalse", new IsFalse());
        VALIDATORS.put("isTruthy", new IsTruthy());
        VALIDATORS.put("isFalsy", new IsFalsy());
        VALIDATORS.put("isNumber", new IsNumber());
        VALIDATORS.put("isString", new IsString());
        VALIDATORS.put("isDate", new IsDate());
        VALIDATORS.put("inRange", new InRange());
    }

    public static WnValidator get(String name) {
        return VALIDATORS.get(name);
    }

    public static WnValidator check(String name) {
        WnValidator vldt = get(name);
        if (null == vldt) {
            throw Er.create("e.validator.NotFound", name);
        }
        return vldt;
    }

    public static boolean test(String name, Object val, Object[] args) {
        WnValidator vldt = check(name);
        return vldt.isTrue(val, args);
    }

    private Map<String, WnValidating> validatings;

    /**
     * @param obj
     *            输入的匹配条件，类似:
     * 
     *            <pre>
     * {
     *    "name": {
     *       name: "!isEqual",
     *       args: ["ZhangZhiHao"]
     *    } 
     * },
     *            </pre>
     * 
     * @return
     */
    public WnValidate(Map<String, Object> map) {
        validatings = new LinkedHashMap<>();
        for (Map.Entry<String, Object> en : map.entrySet()) {
            // 字段
            String key = en.getKey();

            // 读取校验器
            Object val = en.getValue();
            WnValidating vldt = new WnValidating();
            String vldtName;

            // 如果仅仅是字符串
            if (val instanceof CharSequence) {
                vldtName = val.toString();
                vldt.args = new Object[0];
            }
            // 可能又复杂点的参数设置
            else {
                NutMap vlMap = Castors.me().castTo(val, NutMap.class);
                vldtName = vlMap.getString("name");
                // 得到参数
                vldt.args = vlMap.getArray("args", Object.class);
            }

            // 分析校验器名称
            if (vldtName.startsWith("!")) {
                vldt.not = true;
                vldtName = vldtName.substring(1).trim();
            }

            // 尝试获取校验器实例
            vldt.validator = check(vldtName);

            // 计入返回
            validatings.put(key, vldt);
        }
    }

    @SuppressWarnings("unchecked")
    public boolean match(Object obj) {
        if (null == obj) {
            return false;
        }
        // 如果是 Map
        if (obj instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) obj;
            for (Map.Entry<String, WnValidating> en : validatings.entrySet()) {
                String key = en.getKey();
                WnValidating vldt = en.getValue();
                Object val = map.get(key);

                if (!vldt.isTrue(val)) {
                    return false;
                }
            }
            return true;
        }
        // 普通对象
        Mirror<Object> mi = Mirror.me(obj);
        for (Map.Entry<String, WnValidating> en : validatings.entrySet()) {
            String key = en.getKey();
            WnValidating vldt = en.getValue();
            Object val = mi.getValue(obj, key);

            if (!vldt.isTrue(val)) {
                return false;
            }
        }
        return true;

    }

}