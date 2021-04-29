package org.nutz.walnut.ext.net.http.bean;

import org.nutz.walnut.ext.net.http.upload.HttpFormField;
import org.nutz.walnut.util.Ws;

/**
 * 封装一个 multipart form 的字段
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class HttpFormPart extends HttpFormField {

    public static final String STD_INPUT_PATH = ">>INPUT";

    /**
     * 要上传的文件对象路径。如果值为">>INPUT"，则表示这个内容来自标准输入
     * 
     * @see #STD_INPUT_PATH
     */
    private String path;

    /**
     * 表单字段的值。
     */
    private String value;

    public boolean hasPath() {
        return !Ws.isBlank(path);
    }

    public boolean isPathAsStdInput() {
        return STD_INPUT_PATH.equals(path);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
