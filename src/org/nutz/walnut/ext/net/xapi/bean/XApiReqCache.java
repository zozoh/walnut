package org.nutz.walnut.ext.net.xapi.bean;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.Ws;

public class XApiReqCache {
    /**
     * 缓存键是否包含路径
     */
    boolean path;
    /**
     * 缓存键是否包含头
     */
    boolean headers;
    /**
     * 缓存键是否包含参数
     */
    boolean params;
    /**
     * 过期时间从何处取得，通常类似<code>=resp.expires_in</code>
     * <p>
     * 其中 <code>resp</code>是请求返回的内容解析的结果 得到结果通常时间单位为秒，当然也可以在 expiUnit 指定
     */
    String expiIn;

    /**
     * 请求过期的单位，默认是秒，
     *
     * <ul>
     * <li><code>h</code> 小时
     * <li><code>m</code> 分钟
     * <li><code>s</code> 秒
     * </ul>
     */
    String expiIUnit;

    public String getExpiUpdate(NutBean vars) {
        Object du = Wn.explainObj(vars, this.expiIn);
        return String.format("%%ms:now+%s%s", du, Ws.sBlank(this.expiIUnit, "s"));
    }

    public boolean isPath() {
        return path;
    }

    public void setPath(boolean path) {
        this.path = path;
    }

    public boolean isHeaders() {
        return headers;
    }

    public void setHeaders(boolean headers) {
        this.headers = headers;
    }

    public boolean isParams() {
        return params;
    }

    public void setParams(boolean params) {
        this.params = params;
    }

    public String getExpiIn() {
        return expiIn;
    }

    public void setExpiIn(String expiIn) {
        this.expiIn = expiIn;
    }

    public String getExpiIUnit() {
        return expiIUnit;
    }

    public void setExpiIUnit(String expiIUnit) {
        this.expiIUnit = expiIUnit;
    }

}
