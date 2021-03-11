package org.nutz.walnut.ext.xapi;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import javax.imageio.ImageIO;

import org.nutz.castor.Castors;
import org.nutz.http.Request;
import org.nutz.http.Response;
import org.nutz.http.Sender;
import org.nutz.json.Json;
import org.nutz.lang.Encoding;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.lang.Xmls;
import org.nutz.lang.util.LinkedByteBuffer;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.xapi.bean.ThirdXRequest;
import org.nutz.walnut.ext.xapi.impl.DefaultThirdXExpertManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract class AbstractThirdXApi implements ThirdXApi {

    protected ThirdXExpertManager experts;

    protected ThirdXConfigManager configs;

    public AbstractThirdXApi() {
        this(DefaultThirdXExpertManager.getInstance());
    }

    public AbstractThirdXApi(ThirdXExpertManager experts) {
        this.experts = experts;
    }

    public ThirdXRequest prepare(String apiName, String account, String path, NutBean vars) {
        ThirdXRequest req = experts.checkExpert(apiName).check(path);

        // 准备上下文变量
        if (null == vars) {
            vars = new NutMap();
        }

        // 读取密钥
        String ak = configs.loadAccessKey(apiName, account);
        vars.put("@AK", ak);

        // 展开头和参数表
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
        // TODO Nutz 的 Http 客户端写的现在看起来有点乱，应该重写一个
        Request req = Request.get(url);
        Response resp = null;

        // 确定请求：头
        if (xreq.hasHeader()) {
            for (Map.Entry<String, Object> en : xreq.getHeaders().entrySet()) {
                String key = en.getKey();
                Object val = en.getValue();
                if (null != val) {
                    req.header(key, val.toString());
                }
            }
        }
        // 确定请求：参数表
        req.setParams(xreq.getParams());

        // POST 请求
        if (xreq.isPOST()) {
            // 标记请求方法
            req.setMethod(Request.METHOD.POST);

            // 处理一下 QueryString
            if (xreq.hasBody() && xreq.hasParams()) {
                req.setUrl(url + xreq.getParamsAsQueryString(true));
            }

            // 特殊处理一下 post body 的类型
            String bodyType = xreq.getBodyType();

            // XML请求
            if ("xml".equals(bodyType)) {
                String xml = xreq.getBodyDataXml();
                req.header("Content-Type", "text/xml");
                req.setData(xml);
            }
            // JSON 请求
            else if ("json".equals(bodyType)) {
                String json = xreq.getBodyDataJson();
                req.header("Content-Type", "application/json");
                req.setData(json);
            }
              // 普通的 POST 请求，就什么也不用做了
            else {}
        }
          // 其他的情况暂时不支持
        else {
            // TODO 考虑更多情况
        }

        // 设置过期时间
        Sender sender = Sender.create(req);
        sender.setTimeout(xreq.getTimeout());
        sender.setConnTimeout(xreq.getConnectTimeout());

        // 发送请求
        resp = sender.send();

        // 如果出错了
        if (!resp.isOK()) {
            throw new ThirdXException(xreq, "http" + resp.getStatus(), resp.getDetail());
        }

        // 检查一下 响应头
        if (!xreq.isMatch(resp.getHeader().getMap())) {
            String reason = resp.getHeader().toString();
            reason += "\n" + resp.getContent();
            throw new ThirdXException(xreq, "e.xapi.resp.invalid_header", reason);
        }

        // 无论如何，先考虑流
        // 输出成字节数组
        if (classOfT.isArray() && byte.class == classOfT.getComponentType()) {
            LinkedByteBuffer bytes = new LinkedByteBuffer(8192, 10, 1024 * 1024 * 100);
            byte[] buf = new byte[8192];
            int len;
            try {
                InputStream ins = resp.getStream();
                while ((len = ins.read(buf)) >= 0) {
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
                InputStream ins = resp.getStream();
                Reader r = new InputStreamReader(ins, Encoding.UTF8);
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
            return (T) resp.getStream();
        }

        // 准备相应结果
        String dataType = xreq.getDataType();

        // JSON
        if ("json".equals(dataType)) {
            String json = resp.getContent();
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
            String str = resp.getContent();
            return Castors.me().castTo(str, classOfT);
        }

        // XML
        if ("xml".equals(dataType)) {
            String xml = resp.getContent();

            // 要解析成 Document
            if (classOfT.isAssignableFrom(Document.class)) {
                return (T) Xmls.xml(Lang.ins(xml));
            }
            // 要解析成 Element
            if (classOfT.isAssignableFrom(Element.class)) {
                Document doc = Xmls.xml(Lang.ins(xml));
                return (T) doc.getDocumentElement();
            }
            // 要解析成 Map
            if (classOfT.isAssignableFrom(Map.class)) {
                Document doc = Xmls.xml(Lang.ins(xml));
                NutMap map = Xmls.asMap(doc.getDocumentElement());
                return Castors.me().castTo(map, classOfT);
            }
            // 尝试转换
            return Castors.me().castTo(xml, classOfT);
        }

        // 图片
        if ("png".equals(dataType) || "jpeg".equals(dataType)) {
            InputStream ins = resp.getStream();
            // 转成图片对象
            if (classOfT.isAssignableFrom(BufferedImage.class)) {
                try {
                    return (T) ImageIO.read(ins);
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
