package org.nutz.walnut.ext.net.xapi;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import javax.imageio.ImageIO;

import org.nutz.castor.Castors;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Encoding;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.lang.util.LinkedByteBuffer;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.cheap.dom.CheapDocument;
import org.nutz.walnut.cheap.dom.CheapElement;
import org.nutz.walnut.cheap.xml.CheapXmlParsing;
import org.nutz.walnut.ext.net.http.HttpConnector;
import org.nutz.walnut.ext.net.http.HttpContext;
import org.nutz.walnut.ext.net.http.bean.WnHttpResponse;
import org.nutz.walnut.ext.net.xapi.bean.ThirdXRequest;
import org.nutz.walnut.ext.net.xapi.impl.DefaultThirdXExpertManager;
import org.nutz.walnut.util.stream.WnInputStreamFactory;

public abstract class AbstractThirdXApi implements ThirdXApi {

    protected ThirdXExpertManager experts;

    protected ThirdXConfigManager configs;

    public AbstractThirdXApi() {
        this(DefaultThirdXExpertManager.getInstance());
    }

    public AbstractThirdXApi(ThirdXExpertManager experts) {
        this.experts = experts;
    }

    public abstract WnInputStreamFactory getInputStreamFactory();

    public boolean hasValidAccessKey(String apiName, String account) {
        return configs.hasValidAccessKey(apiName, account);
    }

    public ThirdXRequest prepare(String apiName, String account, String path, NutBean vars) {
        ThirdXRequest req = experts.checkExpert(apiName).check(path).clone();

        // 准备上下文变量
        if (null == vars) {
            vars = new NutMap();
        }

        // 读取密钥
        String ak = configs.loadAccessKey(apiName, account, vars, false);
        vars.put("@AK", ak);

        // 展开头和参数表
        req.expalinPath(vars);
        req.explainHeaders(vars);
        req.explainParams(vars);

        // 展开一下 body
        req.explainBody(vars);

        // 返回给调用者，看看它还有没有其他的附加处理逻辑
        return req;
    }

    @SuppressWarnings({"unchecked"})
    public <T> T send(ThirdXRequest xreq, Class<T> classOfT) throws ThirdXException {
        // 准备 URL
        String url = xreq.getBase();
        String path = xreq.getPath();
        if (!url.endsWith("/") && !path.startsWith("/")) {
            url += "/";
        }
        url += path;

        // 准备 HTTP 响应
        HttpContext hc = new HttpContext();
        hc.setUrl(url);
        hc.setFollowRedirects(true);
        hc.setMethod(xreq.getMethod());

        // 确定请求：头
        if (xreq.hasHeader()) {
            hc.setHeaders(xreq.getHeaders());
        }
        // 确定请求：参数表
        hc.setParams(xreq.getParamsWithoutNil());

        // 确定请求：体
        xreq.setupHttpContextBody(hc);

        // 设置过期时间
        hc.setReadTimeout(xreq.getTimeout());
        hc.setConnectTimeout(xreq.getConnectTimeout());
        hc.setInputStreamFactory(this.getInputStreamFactory());

        // 发送请求
        WnHttpResponse resp;
        try {
            HttpConnector c = hc.open();
            c.prepare();
            c.connect();
            c.sendHeaders();
            c.sendBody();

            // 得到响应
            resp = c.getResponse();
        }
        catch (IOException e1) {
            throw new ThirdXException(e1);
        }

        // 如果出错了
        if (!resp.isStatusOk()) {
            String str = resp.getBodyText();
            throw new ThirdXException(xreq, "http" + resp.getStatusCode(), str);
        }

        // 检查一下 响应头
        NutMap respHeaders = resp.getHeaders();
        if (!xreq.isMatch(respHeaders)) {
            String str = resp.getBodyText();
            String reason = Json.toJson(respHeaders, JsonFormat.nice());
            reason += "\n" + str;
            throw new ThirdXException(xreq, "e.xapi.resp.invalid_header", reason);
        }

        // 无论如何，先考虑流
        // 输出成字节数组
        if (classOfT.isArray() && byte.class == classOfT.getComponentType()) {
            LinkedByteBuffer bytes = new LinkedByteBuffer(8192, 10, 1024 * 1024 * 100);
            byte[] buf = new byte[8192];
            int len;
            try {
                while ((len = resp.read(buf)) >= 0) {
                    bytes.write(buf, 0, len);
                }
                return (T) bytes.toArray();
            }
            catch (IOException e) {
                throw Lang.wrapThrow(e);
            }
        }

        // 输出成 Rader
        if (classOfT == Reader.class || classOfT == BufferedReader.class) {
            try {
                Reader r = new InputStreamReader(resp, Encoding.UTF8);
                if (classOfT == BufferedReader.class) {
                    return (T) Streams.buffr(r);
                }
                return (T) r;
            }
            catch (UnsupportedEncodingException e) {
                throw Lang.wrapThrow(e);
            }
        }

        // 输出成 InputStream
        if (classOfT == InputStream.class) {
            return (T) resp;
        }

        // 准备相应结果
        String dataType = xreq.getDataType();

        // JSON
        if ("json".equals(dataType)) {
            String json = resp.getBodyText();
            if (classOfT.isAssignableFrom(String.class)) {
                return (T) json;
            }
            // 其他的类字符串
            if (classOfT.isAssignableFrom(StringBuilder.class)) {
                return (T) new StringBuilder(json);
            }
            // 转换一下
            return Json.fromJson(classOfT, json);
        }

        // Pure Text
        if ("text".equals(dataType)) {
            String str = resp.getBodyText();
            return Castors.me().castTo(str, classOfT);
        }

        // XML
        if ("xml".equals(dataType)) {
            String xml = resp.getBodyText();

            // 要解析成 Document
            if (classOfT.isAssignableFrom(CheapDocument.class)) {
                CheapDocument doc = new CheapDocument(null);
                CheapXmlParsing ing = new CheapXmlParsing(doc);
                doc = ing.parseDoc(xml);
                return (T) doc;
            }
            // 要解析成 Element
            if (classOfT.isAssignableFrom(CheapElement.class)) {
                CheapDocument doc = new CheapDocument(null);
                CheapXmlParsing ing = new CheapXmlParsing(doc);
                doc = ing.parseDoc(xml);
                return (T) doc;
            }
            // 尝试转换
            return Castors.me().castTo(xml, classOfT);
        }

        // 图片
        if ("png".equals(dataType) || "jpeg".equals(dataType)) {
            // 转成图片对象
            if (classOfT.isAssignableFrom(BufferedImage.class)) {
                try {
                    return (T) ImageIO.read(resp);
                }
                catch (IOException e) {
                    throw Lang.wrapThrow(e);
                }
            }
        }

        // 不能支持哦
        throw new ThirdXException(xreq, "resp.convert", dataType);
    }

    @Override
    public ThirdXExpertManager getExpertManager() {
        return experts;
    }

    public void setExperts(ThirdXExpertManager experts) {
        this.experts = experts;
    }

    @Override
    public ThirdXConfigManager getConfigManager() {
        return configs;
    }

    public void setConfigs(ThirdXConfigManager configLoader) {
        this.configs = configLoader;
    }

}
