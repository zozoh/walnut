package org.nutz.walnut.util.bean;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nutz.lang.Lang;
import org.nutz.lang.Mirror;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.bean.val.WnValueType;

public class WnBeanMapping extends HashMap<String, WnBeanField> {

    @SuppressWarnings("unchecked")
    public Object translateAny(Object input, boolean onlyMapping) {
        Mirror<?> mi = Mirror.me(input);
        // ....................................
        // Array
        if (mi.isArray()) {
            int len = Array.getLength(input);
            List<Object> list = new ArrayList<>(len);
            for (int i = 0; i < len; i++) {
                Object val = Array.get(input, i);
                Object v2 = translateAny(val, onlyMapping);
                if (null != v2) {
                    list.add(v2);
                }
            }
            Object[] arr = new Object[list.size()];
            list.toArray(arr);
            return arr;
        }
        // ....................................
        // 集合
        if (mi.isCollection()) {
            Collection<?> col = (Collection<?>) input;
            List<Object> list = new ArrayList<>(col.size());
            for (Object v : col) {
                Object v2 = translateAny(v, onlyMapping);
                if (null != v2) {
                    list.add(v2);
                }
            }
            return list;
        }
        // ....................................
        // Map
        else if (mi.isMap()) {
            NutMap inputMap = NutMap.WRAP((Map<String, Object>) input);
            return translate(inputMap, onlyMapping);
        }
        // 其他的原样返回
        if (onlyMapping) {
            return null;
        }
        return input;
    }

    public NutBean translate(NutBean bean, boolean onlyMapping) {
        NutMap re = new NutMap();

        // 防守
        if (this.isEmpty()) {
            if (onlyMapping) {
                return re;
            }
            return bean;
        }

        for (Map.Entry<String, Object> en : bean.entrySet()) {
            String key = en.getKey();
            Object val = en.getValue();
            try {
                WnBeanField fld = this.get(key);
                // 如果没有声明字段，看看是否直接加入，还是忽略它
                // 这个靠传入的参数 onlyMapping 来指定行为。
                // 譬如 ooml 命令里的 @mapping 就可以用 -only 来开启这个选项
                if (null == fld) {
                    if (onlyMapping) {
                        continue;
                    }
                    re.put(key, val);
                }
                // 执行映射
                else {
                    String k2 = fld.getName(key);
                    Object v2 = fld.tryValueOptions(val);
                    Object v3 = WnValues.toValue(fld, v2);
                    re.put(k2, v3);

                    // 看看还有没有别名字段
                    if (fld.hasAliasFields()) {
                        for (WnBeanField af : fld.getAliasFields()) {
                            // 木有名字，那么无视
                            String ka = af.getName(null);
                            if (Ws.isBlank(ka)) {
                                continue;
                            }
                            Object av3 = WnValues.toValue(af, v3);
                            re.put(ka, av3);
                        }
                    }
                }
            }
            // 搞个容易理解的错误
            catch (Throwable e) {
                throw Er.createf("e.bean.mapping.invalid", "field[%s]: %s", key, e.toString());
            }
        }

        return re;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void setFields(Map<String, Object> fields) {
        this.clear();
        for (Map.Entry en : fields.entrySet()) {
            String key = en.getKey().toString();
            Object val = en.getValue();
            // Map 的话，转换
            if (val instanceof Map) {
                NutMap vo = NutMap.WRAP((Map) val);
                // 处理 eleType
                checkEleType(vo);
                // 转换为字段
                WnBeanField fld = Lang.map2Object(vo, WnBeanField.class);
                this.put(key, fld);
            }
            // String 的话，就是简单映射咯
            else if (val instanceof String) {
                String name = (String) val;
                WnBeanField fld = new WnBeanField();
                fld.setName(name);
                fld.setType(WnValueType.String);
                this.put(key, fld);
            }
        }
    }

    /**
     * 确保自己每个值都是 WnBeanField 对象，有时候从 Json 恢复出来的是 NutMap
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void checkFields() {
        for (Map.Entry en : super.entrySet()) {
            Object val = en.getValue();
            // Map 的话，转换
            if (val instanceof Map) {
                NutMap vo = NutMap.WRAP((Map) val);
                // 处理 eleType
                checkEleType(vo);
                // 转换为字段
                WnBeanField fld = Lang.map2Object(vo, WnBeanField.class);
                en.setValue(fld);
            }
            // String 的话，就是简单映射咯
            else if (val instanceof String) {
                String name = (String) val;
                WnBeanField fld = new WnBeanField();
                fld.setName(name);
                fld.setType(WnValueType.String);
                en.setValue(fld);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void checkEleType(NutMap vo) {
        Object eleType = vo.get("eleType");
        if (null != eleType) {
            // 字符串，就表示类型
            if (eleType instanceof String) {
                WnValue wv = new WnValue();
                wv.setType(WnValueType.valueOf((String) eleType));
                vo.put("eleType", wv);
            }
            // 一个完整的声明
            else if (eleType instanceof Map) {
                NutMap map = NutMap.WRAP((Map<String, Object>) eleType);
                checkEleType(map);
                WnValue wv = Lang.map2Object(map, WnValue.class);
                vo.put("eleType", wv);
            }
            // 其他的移除掉
            else {
                vo.remove("eleType");
            }
        }
    }
}