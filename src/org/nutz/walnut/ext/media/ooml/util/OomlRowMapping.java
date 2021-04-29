package org.nutz.walnut.ext.media.ooml.util;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.util.bean.WnBeanMapping;

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
     * 对象键值映射
     */
    private WnBeanMapping mapping;

    public NutBean toBean(NutBean bean, boolean onlyMapping) {
        return mapping.translate(bean, onlyMapping);
    }

    public void ready() {
        if (null != mapping) {
            mapping.checkFields();
        }
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

    public WnBeanMapping getMapping() {
        return mapping;
    }

    public void setMapping(WnBeanMapping mapping) {
        this.mapping = mapping;
    }

}
