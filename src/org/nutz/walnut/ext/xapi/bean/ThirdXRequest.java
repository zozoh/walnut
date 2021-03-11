package org.nutz.walnut.ext.xapi.bean;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.nutz.http.Http;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Encoding;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.Xmls;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.util.Wlang;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.validate.WnMatch;
import org.nutz.walnut.validate.impl.AlwaysMatch;
import org.nutz.walnut.validate.impl.AutoMatch;
import org.w3c.dom.Document;

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

    private ThirdXMethod method;

    private NutMap headers;

    private NutMap params;

    private String bodyType;

    private Object body;

    private String dataType;

    private NutMap acceptHeader;

    private WnMatch matchHeader;

    public ThirdXRequest() {
        timeout = 0;
        connectTimeout = 0;
        method = ThirdXMethod.GET;
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

    public boolean hasPath() {
        return !Strings.isBlank(path);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String toUrl(boolean ignoreNil) {
        StringBuilder sb = new StringBuilder();
        sb.append(Wn.appendPath(this.base, this.path));
        if (null != this.params) {
            List<String> palist = new ArrayList<>(this.params.size());
            for (Entry<String, Object> en : params.entrySet()) {
                final String key = en.getKey();
                Object val = en.getValue();
                if (val == null) {
                    if (ignoreNil)
                        continue;
                    val = "";
                }
                Wlang.each(val, (index, ele, src) -> {
                    if (ignoreNil) {
                        if (ele == null) {
                            return;
                        }
                        if (ele instanceof CharSequence) {
                            if (Ws.isBlank((CharSequence) ele)) {
                                return;
                            }
                        }
                    }
                    palist.add(String.format("%s=%s",
                                             Http.encode(key, Encoding.UTF8),
                                             Http.encode(ele, Encoding.UTF8)));
                });
            }
            if (!palist.isEmpty()) {
                sb.append('?').append(Ws.join(palist, "&"));
            }
        }
        return sb.toString();
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

    public void setParams(NutMap params) {
        this.params = params;
    }

    public void explainParams(NutBean vars) {
        NutMap pms = (NutMap) Wn.explainObj(vars, this.params);
        this.params = pms;
    }

    public String getParamsAsQueryString(boolean quesMark) {
        StringBuilder sb = new StringBuilder();
        if (this.hasParams()) {
            for (Map.Entry<String, Object> en : params.entrySet()) {
                String key = en.getKey();
                Object val = en.getValue();
                if (sb.length() > 0) {
                    sb.append('&');
                } else if (quesMark) {
                    sb.append('?');
                }
                sb.append(key);
                if (null != val) {
                    try {
                        String v = URLEncoder.encode(val.toString(), Encoding.UTF8);
                        sb.append('=').append(v);
                    }
                    catch (UnsupportedEncodingException e) {}
                }
            }
        }
        return sb.toString();
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

    public boolean hasBody() {
        return null != body;
    }

    public void explainBody(NutBean vars) {
        this.body = Wn.explainObj(vars, this.body);
    }

    public Object getBody() {
        return body;
    }

    @SuppressWarnings("unchecked")
    public String getBodyDataXml() {
        if (null == body) {
            return "";
        }
        // w3c document -> XML
        if (body instanceof Document) {
            try {
                Document doc = (Document) body;
                TransformerFactory transFactory = TransformerFactory.newInstance();
                Transformer transformer;

                transformer = transFactory.newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");

                StringBuilder sb = new StringBuilder();
                OutputStream ops = Lang.ops(sb);
                StreamResult sr = new StreamResult(ops);

                DOMSource ds = new DOMSource(doc);
                transformer.transform(ds, sr);

                return sb.toString();
            }
            catch (TransformerConfigurationException e) {
                throw Lang.wrapThrow(e);
            }
            catch (TransformerException e) {
                throw Lang.wrapThrow(e);
            }
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

}
