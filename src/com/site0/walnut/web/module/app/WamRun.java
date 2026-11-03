package com.site0.walnut.web.module.app;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.lang.Strings;

import com.site0.walnut.login.WnLoginApi;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wn;
import com.site0.walnut.web.bean.WnApp;
import com.site0.walnut.web.impl.WnAppService;
import com.site0.walnut.web.module.AppRespOpsWrapper;
import com.site0.walnut.web.module.HttpRespStatusSetter;
import com.site0.walnut.web.util.WnWeb;

/**
 * 对应入口函数 <code>AppModule.run</code> 的服务类
 *
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnAppModuleRun {

    private WnAppService apps;

    private WnLoginApi auth;

    private String appName;

    private String mimeType;

    private String metaOutputSeparator;

    private String PWD;

    private String cmdText;

    private String in;

    private boolean forceFlushBuffer;

    private boolean logOff;

    private HttpServletRequest req;

    private HttpServletResponse resp;

    public void invoke() throws IOException {
        // 这个接口开放给外部 app 调用
        WnWeb.setCrossDomainHeaders("*", (name, value) -> {
            resp.setHeader(WnWeb.niceHeaderName(name), value);
        });

        // Options 无视
        if (WnWeb.isRequestOptions(req)) {
            return;
        }

        if (logOff) {
            Wn.WC().setLogOff();
        }

        // 找到 app 所在目录
        WnApp app = apps.checkApp(appName, auth);

        // 默认返回的 mime-type 是文本
        if (Strings.isBlank(mimeType))
            mimeType = "text/plain";
        resp.setContentType(mimeType);

        // 准备输出
        HttpRespStatusSetter _resp = new HttpRespStatusSetter(resp);
        AppRespOpsWrapper out = new AppRespOpsWrapper(_resp, 200);
        AppRespOpsWrapper err = new AppRespOpsWrapper(_resp, 500);
        InputStream ins = Strings.isEmpty(in) ? null : Wlang.ins(in);

        // 强制触发响应刷新缓冲
        if (forceFlushBuffer) {
            // 告知 Nginx 代理，不要缓存
            // TODO 看起来木有用
            resp.setHeader("X-ACCEL-BUFFERING", "yes");
            out.setForceFlush(true);
            err.setForceFlush(true);
        }

        // 执行
        apps.runCommand(app, metaOutputSeparator, PWD, cmdText, out, err, ins);
    }

    public WnAppService getApps() {
        return apps;
    }

    public void setApps(WnAppService apps) {
        this.apps = apps;
    }

    public WnLoginApi getAuth() {
        return auth;
    }

    public void setAuth(WnLoginApi auth) {
        this.auth = auth;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getMetaOutputSeparator() {
        return metaOutputSeparator;
    }

    public void setMetaOutputSeparator(String metaOutputSeparator) {
        this.metaOutputSeparator = metaOutputSeparator;
    }

    public String getPWD() {
        return PWD;
    }

    public void setPWD(String PWD) {
        this.PWD = PWD;
    }

    public String getCmdText() {
        return cmdText;
    }

    public void setCmdText(String cmdText) {
        this.cmdText = cmdText;
    }

    public String getIn() {
        return in;
    }

    public void setIn(String in) {
        this.in = in;
    }

    public boolean isForceFlushBuffer() {
        return forceFlushBuffer;
    }

    public void setForceFlushBuffer(boolean forceFlushBuffer) {
        this.forceFlushBuffer = forceFlushBuffer;
    }

    public boolean isLogOff() {
        return logOff;
    }

    public void setLogOff(boolean logOff) {
        this.logOff = logOff;
    }

    public HttpServletRequest getReq() {
        return req;
    }

    public void setReq(HttpServletRequest req) {
        this.req = req;
    }

    public HttpServletResponse getResp() {
        return resp;
    }

    public void setResp(HttpServletResponse resp) {
        this.resp = resp;
    }

}
