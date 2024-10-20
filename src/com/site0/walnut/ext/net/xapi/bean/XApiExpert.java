package com.site0.walnut.ext.net.xapi.bean;

import java.util.HashMap;
import java.util.Map;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.util.Wn;

public class XApiExpert {

    /**
     * 接口名称（即，文件名）
     */
    private String name;

    /**
     * 接口的公共起始路径
     */
    private String base;

    /**
     * 默认调用超时(毫秒)
     */
    private int timeout;

    /**
     * 默认连接超时(毫秒)
     */
    private int connectTimeout;

    /**
     * 配置信息的主目录
     */
    private String home;

    /**
     * 如果请求配置了缓存，缓存在那个路径下。
     * <p>
     * 可以是绝对路径。相对路径的话。则相对于home
     */
    private String apiCachePath;

    /**
     * 配置文件路径
     */
    private String configFilePath;

    /**
     * 存储访问密钥的文件名
     */
    private String accessKeyFilePath;

    /**
     * 【选】动态密钥的获取路径
     * <p>
     * 如果声明了这个，则表示访问密码是动态获取的
     */
    private XApiRequest accessKeyRequest;

    /**
     * 每次生成密钥文件对象设置的元数据
     * <p>
     * 如果是动态请求，那么这个映射表就是如何从响应里取值<br>
     * 否则，就是如何从配置文件中取值<br>
     * 无论怎样，都需要下面三个字段:
     * <ul>
     * <li><code>ticket</code> : 访问密钥的值
     * <li><code>expiTime</code> : 过期时间
     * <li><code>expiTimeUnit</code> : 过期时间的单位 (s|m|h|d|w)
     * </ul>
     */
    private NutMap accessKeyObj;

    /**
     * 当前的接口支持下面这些请求
     * <p>
     * <ul>
     * <li>键为请求的路径（会自动拼合base）
     * <li>值为一个请求对象
     * </ul>
     */
    private Map<String, XApiRequest> requests;

    public XApiExpert() {
        timeout = 3000;
        connectTimeout = 1000;
        accessKeyFilePath = "access_key";
        requests = new HashMap<>();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("[%s] -> %s : %s\n", name, base, home));
        if (null != requests && !requests.isEmpty()) {
            for (String key : requests.keySet()) {
                sb.append(" - ").append(key).append("\n");
            }
        } else {
            sb.append("  ~ empty ~\n");
        }
        return sb.toString();
    }

    public XApiRequest get(String key) {
        XApiRequest req = this.requests.get(key);
        if (null != req) {
            req = req.clone();
            req.setKey(key);
            req.setApiName(this.name);
            req.setBase(this.base);
            req.setDefaultTimeout(timeout);
            req.setDefaultConnectTimeout(connectTimeout);
            return req;
        }
        return null;
    }

    public String getPathInHome(String account, String path, NutBean vars) {
        String homePath = this.getHome();
        String ph = Wn.concatPath(homePath, account, path);
        return Wn.normalizeFullPath(ph, vars);
    }

    public XApiRequest check(String key) {
        XApiRequest req = this.get(key);
        if (null == req) {
            throw Er.createf("e.ThirdXApi.InvalidApiKey", "[%s] %s : %s", name, base, key);
        }
        return req;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getHome() {
        return home;
    }

    public void setHome(String home) {
        this.home = home;
    }

    public String getApiCachePath() {
        return apiCachePath;
    }

    public void setApiCachePath(String apiCachePath) {
        this.apiCachePath = apiCachePath;
    }

    public String getConfigFilePath() {
        return configFilePath;
    }

    public void setConfigFilePath(String configFilePath) {
        this.configFilePath = configFilePath;
    }

    public String getAccessKeyFilePath() {
        return accessKeyFilePath;
    }

    public void setAccessKeyFilePath(String accessKeyFileName) {
        this.accessKeyFilePath = accessKeyFileName;
    }

    /**
     * 如果声明了 "accessKeyRequest"，就是一个动态密钥，<br>
     * 每次过期都要去重新请求三方平台生成。<br>
     * 否则，就是一个模板密钥，直接通过 config + accessKeyObj 渲染生成
     * 
     * @return 是否为动态密钥
     */
    public boolean isDynamicAccessKey() {
        return null != this.accessKeyRequest;
    }

    public XApiRequest getAccessKeyRequest() {
        XApiRequest akr = accessKeyRequest.clone();
        akr.setApiName(name);
        akr.setBase(this.base);
        akr.setDefaultTimeout(timeout);
        akr.setDefaultConnectTimeout(connectTimeout);
        return akr;
    }

    public void setAccessKeyRequest(XApiRequest accessKey) {
        this.accessKeyRequest = accessKey;
    }

    public NutMap getAccessKeyObj() {
        return accessKeyObj;
    }

    public void setAccessKeyObj(NutMap accessKeyObj) {
        this.accessKeyObj = accessKeyObj;
    }

    public Map<String, XApiRequest> getRequests() {
        return requests;
    }

    public void setRequests(Map<String, XApiRequest> requests) {
        this.requests = requests;
    }

}
