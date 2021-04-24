package org.nutz.walnut.ext.ooml.util;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.util.bean.WnBeanMapping;

public class OomlRowMapping {

    /**
     * 存储对象内容的键值（未转换前）
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
