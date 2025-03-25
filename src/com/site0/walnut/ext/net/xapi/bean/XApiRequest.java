package com.site0.walnut.ext.net.xapi.bean;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.castor.Castors;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Encoding;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.Strings;
import org.nutz.lang.stream.VoidInputStream;
import com.site0.walnut.util.tmpl.WnTmpl;
import org.nutz.lang.util.ByteInputStream;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.cheap.dom.CheapDocument;
import com.site0.walnut.ext.net.http.HttpContext;
import com.site0.walnut.ext.net.http.HttpMethod;
import com.site0.walnut.ext.net.http.bean.HttpFormPart;
import com.site0.walnut.ext.net.util.WnNet;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.validate.WnMatch;
import com.site0.walnut.util.validate.impl.AlwaysMatch;
import com.site0.walnut.util.validate.impl.AutoMatch;

/**
 * 封装了第三方 API 的请求逻辑
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class XApiRequest {

    private String apiName;
    private String account;
    private String path;
    private String key;

    /**
     * 如果标识了它为 false， 执行请求的时候，将不会请求 `@AT`
     */
    private boolean needAccountToken;

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

    private HttpMethod method;

    private NutMap headers;

    private NutMap params;

    private XApiBodyType bodyType;

    private Object body;

    private String dataType;

    private XApiReqCache cache;

    private boolean disableCache;

    private NutMap acceptHeader;

    private WnMatch matchHeader;

    public XApiRequest() {
        timeout = 0;
        connectTimeout = 0;
        method = HttpMethod.GET;
        headers = new NutMap();
        params = new NutMap();
        needAccountToken = true;
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

    public XApiRequest clone() {
        XApiRequest req = new XApiRequest();
        req.apiName = apiName;
        req.account = account;
        req.path = path;
        req.key = key;
        req.base = base;
        req.timeout = timeout;
        req.connectTimeout = connectTimeout;
        req.method = method;
        req.headers = headers.duplicate();
        req.params = params.duplicate();
        req.body = body;
        req.bodyType = bodyType;
        req.dataType = dataType;
        req.cache = cache;
        req.acceptHeader = acceptHeader;
        req.matchHeader = matchHeader;
        req.needAccountToken = needAccountToken;
        return req;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
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
        this.path = WnTmpl.exec(this.path, vars);
    }

    public boolean hasKey() {
        return !Ws.isBlank(key);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
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

    public String toUrl() {
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

    public String toCURLCommand(boolean sameLine) {
        List<String> lines = new LinkedList<>();
        // 先搞url
        String url = this.toUrl();
        lines.add("curl " + this.method + " '" + url + "'");

        // header
        if (this.hasHeader()) {
            for (Map.Entry<String, Object> en : this.headers.entrySet()) {
                String key = en.getKey();
                Object val = en.getValue();
                if (null != val) {
                    lines.add(String.format("-H '%s: %s'", Ws.upperFirst(key), val));
                }
            }
        }

        // 再搞body
        if (this.isPOST() && this.hasBody()) {
            lines.add("-d '" + this.getBodyDataJson() + "'");
        }

        if (sameLine) {
            return Ws.join(lines, " ");
        }
        return Ws.join(lines, " \\\n");
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
        return XApiBodyType.json == bodyType;
    }

    public boolean isBodyAsForm() {
        return XApiBodyType.form == bodyType;
    }

    public boolean isBodyAsMultipart() {
        return XApiBodyType.multipart == bodyType;
    }

    public boolean isBodyAsXml() {
        return XApiBodyType.xml == bodyType;
    }

    public boolean isBodyAsText() {
        return XApiBodyType.text == bodyType;
    }

    public boolean isBodyAsBinary() {
        return XApiBodyType.bin == bodyType;
    }

    public boolean hasBodyType() {
        return null != bodyType;
    }

    public XApiBodyType getBodyType() {
        return bodyType;
    }

    public void setBodyType(XApiBodyType bodyType) {
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
        // JSON
        if (this.isBodyAsJson()) {
            String json = this.getBodyDataJson();
            hc.setBody(json);
        }
        // 二进制流或者是纯文本
        else if (this.isBodyAsBinary() || this.isBodyAsText() || !this.hasBodyType()) {
            hc.setBody(this.getBodyInputStream());
        }
        // 普通表单
        else if (this.isBodyAsForm()) {
            hc.setBody(this.getBodyXWWWFormUrlEncoded());
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

    public String getBodyXWWWFormUrlEncoded() {
        if (null == body) {
            return "";
        }
        // 直接就是请求体
        if (body instanceof String) {
            return body.toString();
        }
        // 解析一下
        NutMap map = Wlang.anyToMap(body);
        List<String> list = new ArrayList<>(map.size());
        for (Map.Entry<String, Object> en : map.entrySet()) {
            String key = en.getKey();
            Object val = en.getValue();

            list.add(Ws.encodeUrlPair(key, val));
        }
        return Ws.join(list, "&");
    }

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
            // NutMap map = NutMap.WRAP((Map<String, Object>) body);
            // TODO gen map to XML
            throw Wlang.noImplement();
        }

        throw Wlang.makeThrow("ThirdXRequest: Can not get XML from body");
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

    public boolean isDataAsString() {
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

    public boolean isDataAsJson() {
        return "json".equals(dataType);
    }

    public boolean isDataAsXml() {
        return "xml".equals(dataType);
    }

    public boolean isDataAsText() {
        return "text".equals(dataType);
    }

    private String getCacheKey() {
        if (null == cache) {
            return null;
        }
        // 只有 GET 请求才有必要缓存
        // if (!isGET()) {
        // return null;
        // }
        StringBuilder sb = new StringBuilder();
        if (cache.path) {
            sb.append(this.path);
        }
        if (cache.headers) {
            sb.append("HEADER=");
            _join_key_map(sb, this.headers);
        }
        if (cache.params) {
            sb.append("QS=");
            _join_key_map(sb, this.params);
        }
        if (cache.body) {
            sb.append("BODY=");
            sb.append(Json.toJson(this.body));
        }
        String md5 = Wlang.md5(sb);
        String name = Ws.sBlank(key, "_anonymity");
        return name + "-" + md5;
    }

    public String checkCacheKey() {
        String ck = this.getCacheKey();
        if (Ws.isBlank(ck)) {
            throw Er.createf("e.xapi.NilCacheKey", "%s/%s/%s", apiName, account, path);
        }
        return ck;
    }

    private void _join_key_map(StringBuilder sb, NutBean bean) {
        if (null == bean || bean.isEmpty()) {
            return;
        }
        List<String> keys = new ArrayList<>(bean.size());
        keys.addAll(bean.keySet());
        Collections.sort(keys);
        NutMap map = new NutMap();
        for (String key : keys) {
            Object val = bean.get(key);
            map.put(key, val);
        }
        WnNet.joinQuery(sb, map, false);
    }

    public boolean isCacheEnabled() {
        return null != cache;
    }

    public XApiReqCache getCache() {
        return cache;
    }

    public void setCache(XApiReqCache cache) {
        this.cache = cache;
    }

    public boolean isDisableCache() {
        return disableCache;
    }

    public void setDisableCache(boolean disableCache) {
        this.disableCache = disableCache;
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

    public boolean isNeedAccountToken() {
        return needAccountToken;
    }

    public void setNeedAccountToken(boolean needAccountToken) {
        this.needAccountToken = needAccountToken;
    }

}
