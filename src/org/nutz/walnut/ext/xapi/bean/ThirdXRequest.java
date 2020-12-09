package org.nutz.walnut.ext.xapi.bean;

import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Encoding;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.Xmls;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.util.Wn;
import org.w3c.dom.Document;

/**
 * 封装了第三方 API 的请求逻辑
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class ThirdXRequest {

    private String apiName;

    private String base;

    private int timeout;

    private int connectTimeout;

    private String path;

    private ThirdXMethod method;

    private NutMap headers;

    private NutMap params;

    private String bodyType;

    private Object body;

    private String dataType;

    public ThirdXRequest() {
        timeout = 3000;
        connectTimeout = 1000;
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

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
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
                    String v = URLEncoder.encode(val.toString(), Encoding.CHARSET_UTF8);
                    sb.append('=').append(v);
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

}
