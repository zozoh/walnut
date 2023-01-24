package org.nutz.walnut.ext.net.xapi;

import java.io.IOException;
import java.net.Proxy;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.net.http.HttpConnector;
import org.nutz.walnut.ext.net.http.HttpContext;
import org.nutz.walnut.ext.net.http.bean.WnHttpResponse;
import org.nutz.walnut.ext.net.xapi.bean.XApiRequest;
import org.nutz.walnut.ext.net.xapi.impl.DefaultXApiExpertManager;
import org.nutz.walnut.util.stream.WnInputStreamFactory;

public abstract class AbstractThirdXApi implements XApi {

    protected XApiExpertManager experts;

    protected XApiConfigManager configs;

    private Proxy proxy;

    public AbstractThirdXApi() {
        this(DefaultXApiExpertManager.getInstance());
    }

    public AbstractThirdXApi(XApiExpertManager experts) {
        this.experts = experts;
    }

    public abstract WnInputStreamFactory getInputStreamFactory();

    public boolean hasValidAccessKey(String apiName, String account) {
        return configs.hasValidAccessKey(apiName, account);
    }

    public XApiRequest prepare(String apiName,
                               String account,
                               String path,
                               NutBean vars,
                               boolean force) {
        XApiRequest req = experts.checkExpert(apiName).check(path).clone();

        // 准备上下文变量
        if (null == vars) {
            vars = new NutMap();
        }

        // 读取密钥
        String ak = configs.loadAccessKey(apiName, account, vars, force);
        vars.put("@AK", ak);

        // 设置请求信息
        req.setApiName(apiName);
        req.setAccount(account);

        // 展开头和参数表
        req.expalinPath(vars);
        req.explainHeaders(vars);
        req.explainParams(vars);

        // 展开一下 body
        req.explainBody(vars);

        // 返回给调用者，看看它还有没有其他的附加处理逻辑
        return req;
    }

    public <T> T send(XApiRequest xreq, Class<T> classOfT) throws XApiException {
        // 缓存对象命中就直接返回
        XApiCacheObj cache = configs.loadReqCache(xreq);
        if (cache.isMatched()) {
            return cache.getOutput(classOfT);
        }

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

        // 设置代理
        if (this.hasProxy()) {
            hc.setProxy(proxy);
        }

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
            throw new XApiException(e1);
        }

        // 如果出错了
        if (!resp.isStatusOk()) {
            String str = resp.getBodyText();
            throw new XApiException(xreq, "http" + resp.getStatusCode(), str);
        }

        // 检查一下 响应头
        NutMap respHeaders = resp.getHeaders();
        if (!xreq.isMatch(respHeaders)) {
            String str = resp.getBodyText();
            String reason = Json.toJson(respHeaders, JsonFormat.nice());
            reason += "\n" + str;
            throw new XApiException(xreq, "e.xapi.resp.invalid_header", reason);
        }

        // 输出响应
        return cache.saveAndOutput(resp, classOfT);
    }

    @Override
    public XApiExpertManager getExpertManager() {
        return experts;
    }

    public void setExperts(XApiExpertManager experts) {
        this.experts = experts;
    }

    @Override
    public XApiConfigManager getConfigManager() {
        return configs;
    }

    public void setConfigs(XApiConfigManager configLoader) {
        this.configs = configLoader;
    }

    public boolean hasProxy() {
        return null != proxy;
    }

    public Proxy getProxy() {
        return proxy;
    }

    @Override
    public void setProxy(Proxy proxy) {
        this.proxy = proxy;
    }

}
