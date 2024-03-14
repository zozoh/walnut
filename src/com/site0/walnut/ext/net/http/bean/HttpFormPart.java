package com.site0.walnut.ext.net.http.bean;

import com.site0.walnut.ext.net.http.upload.HttpFormField;

/**
 * 封装一个 multipart form 的字段
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class HttpFormPart extends HttpFormField {

    /**
     * 表单字段的值。如果是上传文件，则表示文件路径
     */
    private String value;

    public boolean hasValue() {
        return null != value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
