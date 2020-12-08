package org.nutz.walnut.ext.xapi.bean;

import java.util.Collection;
import java.util.Map;

import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.w3c.dom.Document;

/**
 * 封装了第三方 API 的请求逻辑
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class ThirdXRequest {

    private String base;

    private String path;

    private ThirdXMethod method;

    private NutMap header;

    private NutMap params;

    private String bodyType;

    private Object body;

    public ThirdXRequest() {
        method = ThirdXMethod.GET;
        header = new NutMap();
        params = new NutMap();
    }

    public ThirdXRequest clone() {
        ThirdXRequest req = new ThirdXRequest();
        req.path = path;
        req.method = method;
        req.header = header.duplicate();
        req.params = params.duplicate();
        req.bodyType = bodyType;
        return req;
    }

    public boolean hasBase() {
        return !Strings.isBlank(base);
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public boolean hasPath() {
        return !Strings.isBlank(path);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isGET() {
        return ThirdXMethod.GET == method;
    }

    public boolean isPOST() {
        return ThirdXMethod.POST == method;
    }

    public boolean isPUT() {
        return ThirdXMethod.PUT == method;
    }

    public boolean isDELETE() {
        return ThirdXMethod.DELETE == method;
    }

    public boolean isPATCH() {
        return ThirdXMethod.PATCH == method;
    }

    public ThirdXMethod getMethod() {
        return method;
    }

    public void setMethod(ThirdXMethod method) {
        this.method = method;
    }

    public boolean hasHeader() {
        return null != header && !header.isEmpty();
    }

    public NutMap getHeader() {
        return header;
    }

    public void setHeader(NutMap header) {
        this.header = header;
    }

    public boolean hasParams() {
        return null != params && !params.isEmpty();
    }

    public NutMap getParams() {
        return params;
    }

    public void setParams(NutMap params) {
        this.params = params;
    }

    public boolean hasBodyType() {
        return !Strings.isBlank(bodyType);
    }

    public String getBodyType() {
        return bodyType;
    }

    public void setBodyType(String bodyType) {
        this.bodyType = bodyType;
    }

    public void setBodyTypeBy(Object body) {
        if (null != body) {
            // 普通表单: application/x-www-form-urlencoded
            if (body instanceof Map<?, ?>) {
                bodyType = "application/x-www-form-urlencoded";
            }
            // TODO 文件流表单: multipart/form-data
            // JSON: application/json
            else if (body instanceof Collection<?> || body.getClass().isArray()) {
                bodyType = "application/json";
            }
            // XML: text/xml
            else if (body instanceof Document) {
                bodyType = "text/xml";
            }
            // 纯文本: text/plain
            else if (body instanceof CharSequence) {
                bodyType = "text/plain";
            }
            // 二进制: application/octet-stream
            else {
                bodyType = "application/octet-stream";
            }
        }
    }

    public boolean hasBody() {
        return null != body;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;

        // 自动判断 bodyType
        if (!this.hasBodyType()) {
            this.setBodyTypeBy(body);
        }
    }

}
