package com.site0.walnut.ext.media.ooml.util;

import java.util.Map;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.util.bean.WnBeanMapping;
import com.site0.walnut.util.validate.WnMatch;
import com.site0.walnut.util.validate.impl.AutoMatch;

public class OomlRowMapping {

    /**
     * 存储对象内容的键值（未转换前）
     * <p>
     * 一个对象的data靠什么写入呢？也许是数据行的一个单元格的内容。<br>
     * 那么到底是哪个单元格呢？这里是一个列名（未进行映射前，也就是表格原始的行头）
     * 从这个值，可以得到那个单元格的内容，可能是一段普通文本，也可能是个<code>XlsxMedia</code>
     */
    private String content;

    private boolean contentAsImage;

    /**
     * 指定了一个 AutoMatch，被其匹配的对象，将在转换时被无视
     */
    private Object ignore;

    private WnMatch __wm_ignore;

    /**
     * 指定了一个 AutoMatch，如果有声明，那么没有被其匹配的对象，将在转换时被无视
     */
    private Object filter;

    private WnMatch __wm_filter;

    /**
     * 转换后，增加一系列默认字段
     */
    private NutMap defaultMeta;

    /**
     * 转换后，强制覆盖一系列字段
     */
    private NutMap overrideMeta;

    /**
     * 对象键值映射
     */
    private WnBeanMapping mapping;

    public NutBean toBean(NutBean bean, boolean onlyMapping) {
        NutBean re = mapping.translate(bean, onlyMapping);
        // 是否无视
        if (!__wm_filter.match(re)) {
            return null;
        }
        if (__wm_ignore.match(re)) {
            return null;
        }
        // 设置默认值
        if (this.hasDefaultMeta()) {
            for (Map.Entry<String, Object> en : defaultMeta.entrySet()) {
                String key = en.getKey();
                Object val = en.getValue();
                re.putDefault(key, val);
            }
        }
        // 强制覆盖
        if (this.hasOverrideMeta()) {
            re.putAll(overrideMeta);
        }

        // 搞定
        return re;
    }

    public void ready(WnIo io, NutBean vars, Map<String, NutMap[]> caches) {
        if (null != mapping) {
            mapping.checkFields(io, vars, caches);
        }
        __wm_filter = AutoMatch.parse(filter, true);
        __wm_ignore = AutoMatch.parse(ignore, false);
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isContentAsImage() {
        return contentAsImage;
    }

    public void setContentAsImage(boolean contentAsImage) {
        this.contentAsImage = contentAsImage;
    }

    public boolean hasDefaultMeta() {
        return null != this.defaultMeta && !defaultMeta.isEmpty();
    }

    public NutMap getDefaultMeta() {
        return defaultMeta;
    }

    public void setDefaultMeta(NutMap defaultMeta) {
        this.defaultMeta = defaultMeta;
    }

    public boolean hasOverrideMeta() {
        return null != overrideMeta && !overrideMeta.isEmpty();
    }

    public NutMap getOverrideMeta() {
        return overrideMeta;
    }

    public void setOverrideMeta(NutMap overrideMeta) {
        this.overrideMeta = overrideMeta;
    }

    /**
     * @param keys 过滤字段的映射键
     * @param names 过滤字段的输出名
     */
    public void setPickingFields(WnMatch keys, WnMatch names) {
        if (null != this.mapping) {
            this.mapping.setPickKeys(keys);
            this.mapping.setPickNames(names);
        }
    }

    public WnBeanMapping getMapping() {
        return mapping;
    }

    public void setMapping(WnBeanMapping mapping) {
        this.mapping = mapping;
    }

}
