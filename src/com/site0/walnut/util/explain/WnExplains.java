package com.site0.walnut.util.explain;

import java.util.Collection;
import java.util.List;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.castor.Castors;
import org.nutz.lang.Mirror;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.lang.util.Regex;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Ws;

public class WnExplains {

    public static Object explainAny(WnExplain ex, Object any) {
        if (null == any) {

        }
        // 数组
        if (any.getClass().isArray()) {
            int N = Wlang.count(any);
            Object[] arr = new Object[N];
            for (int i = 0; i < N; i++) {
                Object v = Array.get(any, i);
                NutMap map = Wlang.anyToMap(v);
                Object v2 = ex.explain(map);
                arr[i] = v2;
            }
            return arr;

        }
        // 集合
        else if (any instanceof Collection<?>) {
            Collection<?> col = (Collection<?>) any;
            List<Object> list = new ArrayList<>(col.size());
            for (Object ele : col) {
                NutMap map = Wlang.anyToMap(ele);
                Object v2 = ex.explain(map);
                list.add(v2);
            }
            return list;
        }
        // 对象
        else {
            NutMap map = Wlang.anyToMap(any);
            return ex.explain(map);
        }
    }

    private static final Pattern EO1 = Regex.getPattern("^:(:*(=|==|!=|->)(.+))$");
    private static final Pattern EO2 = Regex.getPattern("^([-=]>)(.+)$");
    private static final Pattern EO3 = Regex.getPattern("^(==?|!=)([^?]+)(\\?(.*))?$");
    private static final Pattern EO4 = Regex.getPattern("^(([\\w\\d_.]+)\\?\\?)?(.+)$");

    /**
     * 预先解析一个展开一个对象。可以是字符串，数组，集合，Map 等
     * 
     * @param obj
     *            要被展开的对象
     * @return 对象展开器
     */
    @SuppressWarnings("unchecked")
    public static WnExplain parse(Object obj) {
        // 防守
        if (null == obj)
            return null;

        Mirror<?> mi = Mirror.me(obj);
        // ....................................
        // String
        if (mi.isStringLike()) {
            String str = obj.toString();
            // Escape
            Matcher m = EO1.matcher(str);
            if (m.find()) {
                return new WnExplainStatic(m.group(1));
            }

            String m_type = null, m_val = null;
            Object m_dft = null;
            m = EO2.matcher(str);
            if (m.find()) {
                m_type = m.group(1);
                m_val = Strings.trim(m.group(2));
            }
            // Find key in context
            else {
                m = EO3.matcher(str);
                if (m.find()) {
                    m_type = m.group(1);
                    m_val = Strings.trim(m.group(2));

                    // 搞默认值，聪明点，根据值的样子改变对象类型，不要傻傻的做成字符串
                    String dft = Strings.trim(m.group(4));
                    m_dft = dft;
                    // starts with "=" auto covert to JS value
                    if (dft != null) {
                        // 确定要变java 值
                        if (dft.startsWith("=")) {
                            m_dft = Ws.toJavaValue(dft.substring(1).trim());
                        }
                        // 布尔的一定要去掉
                        else if ("==".equals(m_type) || "!=".equals(m_type)) {
                            m_dft = Castors.me().castTo(dft, boolean.class);
                        }
                        // 其他的去掉空白
                        else {
                            m_dft = Ws.trim(dft);
                        }
                    }
                }
            }
            // Matched
            if (null != m_type) {
                // ==xxx # Get Boolean value now
                if ("==".equals(m_type)) {
                    return new WnExplainBoolValue(m_val, m_dft);
                }
                // !=xxx # Revert Boolean value now
                if ("!=".equals(m_type)) {
                    return new WnExplainBoolValue(m_val, m_dft, true);
                }
                // =xxx # Get Value Now
                if ("=".equals(m_type)) {
                    if ("..".equals(m_val)) {
                        return new WnExplainWholeContext();
                    }
                    return new WnExplainGet(m_val, m_dft);
                }
                // => Call EL
                if ("=>".equals(m_type)) {
                    return new WnExplainCall(m_val);
                }
                // Render template
                if ("->".equals(m_type)) {
                    String test = null;
                    String tmpl = m_val;
                    Matcher m2 = EO4.matcher(m_val);
                    if (m2.find()) {
                        test = m2.group(2);
                        tmpl = m2.group(3);
                    }
                    return new WnExplainTmpl(tmpl, test);
                }
            }
        }
        // ....................................
        // Array
        if (mi.isArray()) {
            return new WnExplainArray(obj, true, null);
        }
        // ....................................
        // 集合
        if (mi.isCollection()) {
            return new WnExplainArray(obj, null);
        }
        // ....................................
        // Map
        else if (mi.isMap()) {
            NutMap map = NutMap.WRAP((Map<String, Object>) obj);

            // 仅仅是映射
            String valKey = map.getString("key");
            Object mapping = map.get("mapping");
            Object dftValue = map.get("dft");
            if (!Strings.isBlank(valKey) && null != mapping && (mapping instanceof Map)) {
                return new WxExplainValMapping(valKey, (Map<String, Object>) mapping, dftValue);
            }

            // 递归
            return new WnExplainMap(map);
        }
        // 其他就直接返回了
        return new WnExplainStatic(obj);
    }

}
