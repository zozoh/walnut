package org.nutz.walnut.util.bean;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.nutz.lang.Lang;
import org.nutz.lang.Mirror;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.bean.val.WnValueType;

public class WnBeanMapping extends LinkedHashMap<String, WnBeanField> {

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

        // 只有映射
        if (onlyMapping) {
            for (Map.Entry<String, WnBeanField> en : this.entrySet()) {
                String key = en.getKey();
                WnBeanField fld = en.getValue();

                // 是否忽略 visible/hidden
                if (fld.isIgnore(bean)) {
                    continue;
                }

                Object val;
                // 获取值: 条件选择
                if (fld.hasMatchValue()) {
                    val = fld.tryMatchValue(bean);
                }
                // 获取值：直接取值
                else {
                    val = bean.get(key);
                }

                // 无视空
                if (null == val) {
                    continue;
                }

                // 执行映射
                __map_bean_field_val(re, fld, val, key);
            }
        }
        // 全部方式
        else {
            for (Map.Entry<String, Object> en : bean.entrySet()) {
                String key = en.getKey();
                Object val = en.getValue();

                WnBeanField fld = this.get(key);
                // 未声明映射字段，直接 copy
                if (null == fld) {
                    re.put(key, val);
                }
                // 执行映射
                else {
                    __map_bean_field_val(re, fld, val, key);
                }

            }
        }

        return re;
    }

    private void __map_bean_field_val(NutMap re, WnBeanField fld, Object val, String key) {
        try {
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
                    Object av = val;
                    if (af.isUseMappedValue()) {
                        av = v3;
                    }
                    Object av2 = af.tryValueOptions(av);
                    Object av3 = WnValues.toValue(af, av2);
                    re.put(ka, av3);
                }
            }
        }
        // 搞个容易理解的错误
        catch (Throwable e) {
            throw Er.createf("e.bean.mapping.invalid", "field[%s]: %s", key, e.toString());
        }
    }

    public void setFields(Map<String, Object> fields,
                          WnIo io,
                          NutBean vars,
                          Map<String, NutMap[]> caches) {
        this.clear();
        for (Map.Entry<String, Object> en : fields.entrySet()) {
            WnBeanField fld = transEntryToField(en, io, vars, caches);
            if (null != fld) {
                String key = en.getKey().toString();
                this.put(key, fld);
            }
        }
    }

    public void setFields(Map<String, Object> fields, WnIo io, NutBean vars) {
        Map<String, NutMap[]> caches = new HashMap<>();
        this.setFields(fields, io, vars, caches);
    }

    public void setFields(Map<String, Object> fields, WnSystem sys) {
        WnIo io = sys.io;
        NutBean vars = sys.session.getVars();
        this.setFields(fields, io, vars);
    }

    public void loadFrom(String path, WnIo io, NutBean vars, Map<String, NutMap[]> caches) {
        String aph = Wn.normalizeFullPath(path, vars);
        WnObj o = io.check(null, aph);
        NutMap map = io.readJson(o, NutMap.class);
        this.setFields(map, io, vars, caches);
    }

    public void loadFrom(String path, WnIo io, NutBean vars) {
        Map<String, NutMap[]> caches = new HashMap<>();
        this.loadFrom(path, io, vars, caches);
    }

    public void loadFrom(String path, WnSystem sys) {
        WnIo io = sys.io;
        NutBean vars = sys.session.getVars();
        this.loadFrom(path, io, vars);
    }

    /**
     * 确保自己每个值都是 WnBeanField 对象，有时候从 Json 恢复出来的是 NutMap
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void checkFields(WnIo io, NutBean vars, Map<String, NutMap[]> caches) {
        for (Map.Entry en : super.entrySet()) {
            WnBeanField fld = transEntryToField(en, io, vars, caches);
            if (null != fld) {
                en.setValue(fld);
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private WnBeanField transEntryToField(Map.Entry en,
                                          WnIo io,
                                          NutBean vars,
                                          Map<String, NutMap[]> caches) {
        WnBeanField fld = null;
        Object val = en.getValue();
        // Map 的话，转换
        if (val instanceof Map) {
            NutMap vo = NutMap.WRAP((Map) val);
            // 处理 eleType
            checkEleType(vo, io, vars, caches);
            // 转换为字段
            try {
                fld = Lang.map2Object(vo, WnBeanField.class);
                fld.loadOptions(io, vars, caches);
                if (fld.hasMapping()) {
                    fld.getMapping().checkFields(io, vars, caches);
                }
            }
            // 捕获异常，打印更完整的信息
            catch (Exception e) {
                throw Er.create(e, "e.bean.mapping.checkFields", en.getKey());
            }
        }
        // String 的话，就是简单映射咯
        else if (val instanceof String) {
            String name = (String) val;
            fld = new WnBeanField();
            fld.setName(name);
            fld.setType(WnValueType.String);
        }
        return fld;
    }

    @SuppressWarnings("unchecked")
    private void checkEleType(NutMap vo, WnIo io, NutBean vars, Map<String, NutMap[]> caches) {
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
                checkEleType(map, io, vars, caches);
                WnValue wv = Lang.map2Object(map, WnValue.class);
                wv.loadOptions(io, vars, caches);
                if (wv.hasMapping()) {
                    wv.getMapping().checkFields(io, vars, caches);
                }
                vo.put("eleType", wv);
            }
            // 其他的移除掉
            else {
                vo.remove("eleType");
            }
        }
    }
}
