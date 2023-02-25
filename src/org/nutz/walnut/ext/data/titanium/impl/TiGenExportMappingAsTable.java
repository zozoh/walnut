package org.nutz.walnut.ext.data.titanium.impl;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.data.titanium.api.TiGenMapping;
import org.nutz.walnut.util.Wlang;

public class TiGenExportMappingAsTable extends TiGenMapping {

    @Override
    protected void joinField(NutMap field) {
        String title = field.getString("title");
        boolean candidate = field.getBoolean("candidate");
        Object display = field.get("display");
        if (null == display) {
            return;
        }

        // 兼容考虑成组字段&普通字段名
        // 数组
        if (display.getClass().isArray()) {
            int len = Array.getLength(display);
            for (int i = 0; i < len; i++) {
                NutMap dis = toStdDisplay(Array.get(display, i));
                _join_display(title, dis, len > 1, candidate);
            }
        }
        // 集合
        else if (display instanceof Collection<?>) {
            Collection<?> col = (Collection<?>) display;
            int len = col.size();
            for (Object ele : col) {
                NutMap dis = toStdDisplay(ele);
                _join_display(title, dis, len > 1, candidate);
            }
        }
        // 就是一个对象咯
        else {
            NutMap dis = toStdDisplay(display);
            _join_display(title, dis, false, candidate);
        }

    }

    private void _join_display(String title, NutMap dis, boolean multiDisplay, boolean candidate) {
        Object key = dis.get("key");
        if (null == key) {
            return;
        }
        if (key.getClass().isArray()) {
            int len = Array.getLength(key);
            for (int i = 0; i < len; i++) {
                String k = Array.get(key, i).toString();
                String ftitle = multiDisplay || len > 0 ? title + "_" + k : title;
                boolean asDft = this.isDefault(k, candidate);
                __join_one_display_key(k, ftitle, asDft, dis);
            }
        }
        // 集合
        else if (key instanceof Collection<?>) {
            Collection<?> col = (Collection<?>) key;
            int len = col.size();
            for (Object ele : col) {
                String k = ele.toString();
                String ftitle = multiDisplay || len > 0 ? title + "_" + k : title;
                boolean asDft = this.isDefault(k, candidate);
                __join_one_display_key(k, ftitle, asDft, dis);
            }
        }
        // 就是一个对象咯
        else {
            String k = key.toString();
            boolean asDft = this.isDefault(k, candidate);
            __join_one_display_key(k, title, asDft, dis);
        }
    }

    protected void __join_one_display_key(String key, String ftitle, boolean asDft, NutMap dis) {
        NutMap v = new NutMap();
        v.put("name", ftitle);
        if (asDft) {
            v.put("asDefault", asDft);
        }
        // 记入映射
        this.putFieldMapping(key, v);
    }

    /**
     * @param display
     *            可以是字符串或者对象
     * @return 标准单元格显示对象
     */
    @SuppressWarnings("unchecked")
    private NutMap toStdDisplay(Object display) {
        if (null == display) {
            return null;
        }
        if (display instanceof String) {
            return Wlang.map("name", display);
        }
        if (display instanceof Map) {
            return NutMap.WRAP((Map<String, Object>) display);
        }
        return null;
    }

}
