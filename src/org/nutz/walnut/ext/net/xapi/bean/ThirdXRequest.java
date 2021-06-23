package org.nutz.walnut.ext.net.xapi.bean;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.castor.Castors;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Encoding;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.Xmls;
import org.nutz.lang.stream.VoidInputStream;
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.ByteInputStream;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.cheap.dom.CheapDocument;
import org.nutz.walnut.ext.net.http.HttpContext;
import org.nutz.walnut.ext.net.http.HttpMethod;
import org.nutz.walnut.ext.net.http.bean.HttpFormPart;
import org.nutz.walnut.ext.net.util.WnNet;
import org.nutz.walnut.util.Wlang;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.validate.WnMatch;
import org.nutz.walnut.util.validate.impl.AlwaysMatch;
import org.nutz.walnut.util.validate.impl.AutoMatch;

/**
 * 封装了第三方 API 的请求逻辑
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class ThirdXRequest {

    private String apiName;

    /**
     * 接口的公共起始路径
     */
    private String base;

    /**
     * 调用超时(毫秒)
     */
    private int timeout;

    /**
     * 连接超时(毫秒)
     */
    private int connectTimeout;

    private String path;

    private HttpMethod method;

    private NutMap headers;

    private NutMap params;

    private ThirdXBodyType bodyType;

    private Object body;

    private String dataType;

    private NutMap acceptHeader;

    private WnMatch matchHeader;

    public ThirdXRequest() {
        timeout = 0;
        connectTimeout = 0;
        method = HttpMethod.GET;
        headers = new NutMap();
        params = new NutMap();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (null != this.method) {
            sb.append(method.toString()).append(" ");
        }
        sb.append(base).append(path);

        if (null != params) {
            sb.append(" ::");
            sb.append(Json.toJson(params));
        }

        if (null != bodyType) {
            sb.append(" [").append(bodyType).append("]");
        }

        if (null != dataType) {
            sb.append(" =>").append(dataType);
        }

        return sb.toString();
    }

    public ThirdXRequest clone() {
        ThirdXRequest req = new ThirdXRequest();
        req.apiName = apiName;
        req.base = base;
        req.timeout = timeout;
        req.connectTimeout = connectTimeout;
        req.path = path;
        req.method = method;
        req.headers = headers.duplicate();
        req.params = params.duplicate();
        req.body = body;
        req.bodyType = bodyType;
        req.dataType = dataType;
        req.acceptHeader = acceptHeader;
        req.matchHeader = matchHeader;
        return req;
    }

    public boolean hasApiName() {
        return !Strings.isBlank(apiName);
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
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

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public void setDefaultTimeout(int timeout) {
        if (this.timeout <= 0 && timeout > 0)
            this.timeout = timeout;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public void setDefaultConnectTimeout(int connectTimeout) {
        if (this.connectTimeout <= 0 && connectTimeout > 0)
            this.connectTimeout = connectTimeout;
    }

    public void expalinPath(NutBean vars) {
        this.path = Tmpl.exec(this.path, vars);
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

    private static final Pattern _P = Pattern.compile("^(https?://)(.+)$");

    public String toUrl(boolean ignoreNil) {
        // 先搞一下 URL，为了防止 base 和 path 之间重复的 `//`
        // 就有了下面这个有点不好看的代码
        // 不这么写，直接 Wn.appendPath 会导致 `http://` 变成 `http:/`
        StringBuilder sb = new StringBuilder();
        Matcher m = _P.matcher(this.base);
        String protocal = null;
        String base = null;
        if (m.find()) {
            protocal = m.group(1);
            base = m.group(2);
        } else {
            base = this.base;
        }
        if (null != protocal) {
            sb.append(protocal);
        }
        sb.append(Wn.appendPath(base, this.path));
        //
        // 处理参数
        //
        if (this.isGET() && null != this.params && !this.params.isEmpty()) {
            sb.append('?');
            WnNet.joinQuery(sb, this.params, true, true);
        }
        return sb.toString();
    }

    public boolean isGET() {
        return HttpMethod.GET == method;
    }

    public boolean isPOST() {
        return HttpMethod.POST == method;
    }

    public boolean isPUT() {
        return HttpMethod.PUT == method;
    }

    public boolean isDELETE() {
        return HttpMethod.DELETE == method;
    }

    public boolean isPATCH() {
        return HttpMethod.PATCH == method;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public boolean hasHeader() {
        return null != headers && !headers.isEmpty();
    }

    public NutMap getHeaders() {
        return headers;
    }

    public void setHeaders(NutMap headers) {
        this.headers = headers;
    }

    public void explainHeaders(NutBean vars) {
        NutMap hds = (NutMap) Wn.explainObj(vars, this.headers);
        this.headers = hds;
    }

    public boolean hasParams() {
        return null != params && !params.isEmpty();
    }

    public NutMap getParams() {
        return params;
    }

    public NutBean getParamsWithoutNil() {
        NutMap map = new NutMap();
        if (null != params) {
            for (Map.Entry<String, Object> en : params.entrySet()) {
                String key = en.getKey();
                Object val = en.getValue();
                if (null != val) {
                    map.put(key, val);
                }
            }
        }
        return map;
    }

    public void setParams(NutMap params) {
        this.params = params;
    }

    public void explainParams(NutBean vars) {
        NutMap pms = (NutMap) Wn.explainObj(vars, this.params);
        this.params = pms;
    }

    public boolean isBodyAsJson() {
        return ThirdXBodyType.json == bodyType;
    }

    public boolean isBodyAsForm() {
        return ThirdXBodyType.form == bodyType;
    }

    public boolean isBodyAsMultipart() {
        return ThirdXBodyType.multipart == bodyType;
    }

    public boolean isBodyAsXml() {
        return ThirdXBodyType.xml == bodyType;
    }

    public boolean isBodyAsText() {
        return ThirdXBodyType.text == bodyType;
    }

    public boolean isBodyAsBinary() {
        return ThirdXBodyType.bin == bodyType;
    }

    public boolean hasBodyType() {
        return null != bodyType;
    }

    public ThirdXBodyType getBodyType() {
        return bodyType;
    }

    public void setBodyType(ThirdXBodyType bodyType) {
        this.bodyType = bodyType;
    }

    public boolean hasBody() {
        return null != body;
    }

    public void explainBody(NutBean vars) {
        this.body = Wn.explainObj(vars, this.body);
    }

    public Object getBody() {
        return body;
    }

    public void setupHttpContextBody(HttpContext hc) {
        if (!this.isPOST() || null == body) {
            return;
        }
        // 二进制流或者是纯文本
        if (this.isBodyAsBinary() || this.isBodyAsText() || !this.hasBodyType()) {
            hc.setBody(this.getBodyInputStream());
        }
          // 普通表单
        else if (this.isBodyAsForm()) {
            // 无视 body
        }
        // 文件上传流
        else if (this.isBodyAsMultipart()) {
            List<HttpFormPart> parts = new LinkedList<>();
            Wlang.each(this.body, (index, ele, src) -> {
                HttpFormPart part = Castors.me().castTo(ele, HttpFormPart.class);
                parts.add(part);
            });
            hc.setFormParts(parts);
        }
        // XML
        else if (this.isBodyAsXml()) {
            String xml = this.getBodyDataXml();
            hc.setBody(xml);
        }
        // JSON
        else if (this.isBodyAsJson()) {
            String json = this.getBodyDataJson();
            hc.setBody(json);
        }
        // 其他的不支持
        else {
            throw Er.create("e.thridx.req.UnSupportedBodyType", this.bodyType);
        }
    }

    public InputStream getBodyInputStream() {
        if (null == body) {
            return new VoidInputStream();
        }
        if (body instanceof InputStream) {
            return (InputStream) body;
        }
        if (body instanceof byte[]) {
            byte[] bs = (byte[]) body;
            return new ByteInputStream(bs);
        }
        String str = body.toString();
        byte[] bs = str.getBytes(Encoding.CHARSET_UTF8);
        return new ByteInputStream(bs);
    }

    @SuppressWarnings("unchecked")
    public String getBodyDataXml() {
        if (null == body) {
            return "";
        }
        // XML
        if (body instanceof CheapDocument) {
            ((CheapDocument) body).toMarkup();
        }
        // Pure XML code
        if (body instanceof CharSequence) {
            return body.toString();
        }
        // Map -> XML
        if (body instanceof Map) {
            NutMap map = NutMap.WRAP((Map<String, Object>) body);
            return Xmls.mapToXml(map);
        }

        throw Lang.makeThrow("ThirdXRequest: Can not get XML from body");
    }

    public String getBodyDataJson() {
        if (null == body) {
            return "";
        }
        if (body instanceof CharSequence) {
            return body.toString();
        }
        return Json.toJson(body, JsonFormat.compact().setQuoteName(true));
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public boolean isDataAsBinary() {
        return "bin".equals(dataType);
    }

    public boolean isDataAsImage() {
        return dataType.matches("^(png|jpeg)$");
    }

    public boolean isDataAsBinStream() {
        return this.isDataAsBinary() && this.isDataAsImage();
    }

    public boolean isDataAsText() {
        return dataType.matches("^(text|json|xml)$");
    }

    public boolean hasDataType() {
        return null != dataType;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public NutMap getAcceptHeader() {
        return acceptHeader;
    }

    public void setAcceptHeader(NutMap acceptHeader) {
        this.acceptHeader = acceptHeader;
        boolean hasHeader = null == acceptHeader || acceptHeader.isEmpty();
        this.matchHeader = hasHeader ? new AlwaysMatch(true) : new AutoMatch(acceptHeader);
    }

    public boolean isMatch(NutBean header) {
        if (null == this.matchHeader)
            return true;
        return this.matchHeader.match(header);
    }

    public WnMatch getMatchHeader() {
        return matchHeader;
    }

    public void setMatchHeader(WnMatch matchHeader) {
        this.matchHeader = matchHeader;
    }

}
