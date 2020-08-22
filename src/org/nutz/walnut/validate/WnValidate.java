package org.nutz.walnut.validate;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.nutz.castor.Castors;
import org.nutz.lang.Lang;
import org.nutz.lang.Mirror;
import org.nutz.lang.util.DateRegion;
import org.nutz.lang.util.NutMap;
import org.nutz.lang.util.Region;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.util.WnRg;
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
        VALIDATORS.put("isMatch", new IsMatch());
        VALIDATORS.put("isEqual", new IsEqual());
        VALIDATORS.put("isOf", new IsOf());
        VALIDATORS.put("matchRegex", new MatchRegex());
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

            // 无视空
            if (null == val) {
                continue;
            }

            // 准备分析
            Mirror<?> mi = Mirror.me(val);
            String vldtName;
            WnValidating vldt = new WnValidating();

            // 如果仅仅是字符串
            if (mi.isStringLike()) {
                String str = val.toString();
                // 正则表达式
                if (str.startsWith("^")) {
                    vldtName = "matchRegex";
                    vldt.args = Lang.array(str);
                }
                // 就是字符串
                else if (str.startsWith("=")) {
                    vldtName = "isEqual";
                    vldt.args = Lang.array(str.substring(1));
                }
                // 整数范围
                else if (str.matches(WnRg.intRegion())) {
                    vldtName = "inRange";
                    vldt.args = Lang.array(Region.Int(str));
                }
                // 长整数范围
                else if (str.matches(WnRg.longRegion())) {
                    vldtName = "inRange";
                    vldt.args = Lang.array(Region.Long(str));
                }
                // 浮点范围
                else if (str.matches(WnRg.floatRegion())) {
                    vldtName = "inRange";
                    vldt.args = Lang.array(Region.Float(str));
                }
                // 日期范围
                else if (str.matches(WnRg.dateRegion("^"))) {
                    vldtName = "inRange";
                    vldt.args = Lang.array(Region.Date(str));
                }
                // 日期范围当做毫秒数
                else if (str.matches(WnRg.dateRegion("^[Mm][Ss]"))) {
                    String s = str.substring(2);
                    DateRegion rg = Region.Date(s);

                    vldtName = "inRange";
                    vldt.args = Lang.array(rg);
                }
                // 其他，就当作验证器名称吧
                else {
                    vldtName = val.toString();
                    vldt.args = new Object[0];
                }
            }
            // 如果是简单对象
            else if (mi.isSimple()) {
                vldtName = "isEqual";
                vldt.args = Lang.array(val);
            }
            // 数组
            else if (val.getClass().isArray()) {
                vldtName = Lang.first(val).toString();
                int len = Lang.eleSize(val);
                vldt.args = new Object[len - 1];
                System.arraycopy(val, 1, vldt.args, 0, len);
            }
            // 集合
            else if (val instanceof Collection<?>) {
                Collection<?> col = (Collection<?>) val;
                int len = col.size();
                Iterator<?> it = col.iterator();
                vldtName = it.next().toString();
                vldt.args = new Object[len - 1];
                int i = 0;
                while (it.hasNext()) {
                    vldt.args[i++] = it.next();
                }
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
