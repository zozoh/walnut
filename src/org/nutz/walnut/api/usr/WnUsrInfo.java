package org.nutz.walnut.api.usr;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;

/**
 * 这个帮助类用来记录创建/登录/获取用户时的信息
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnUsrInfo {

    public WnUsrInfo() {}

    public WnUsrInfo(String str) {
        this.valueOf(str);
    }

    // ................................................
    // 采用密码登录 : "xxxx"
    // 登录的字符串可以是邮箱，电话号码，或者用户名
    private String loginStr;

    private String loginPassword;

    private String phone;

    private String email;

    private String name;

    // ................................................
    // oauth 认证登录
    // oauth:github:profileId
    private String oauthProvider;

    private String oauthProfileId;

    // ................................................
    // 公众号 : "wxgh:weixinPnb:openid
    private String weixinPNB;

    private String weixinOpenId;

    private static final Pattern _P = Pattern.compile("^(oauth|wxgh):([^:]+):(.+)$");

    public WnUsrInfo valueOf(String str) {
        Matcher m = _P.matcher(str);
        // 特殊类型登录
        if (m.find()) {
            String md = m.group(1);
            String pvd = m.group(2);
            String val = m.group(3);
            // Github
            if ("oauth".equals(md)) {
                this.setOauthProvider(pvd);
                this.setOauthProfileId(val);
            }
            // 某个微信公众号
            else if ("wxgh".equals(md)) {
                this.setWeixinPNB(pvd);
                this.setWeixinOpenId(val);
            }
        }
        // 采用登录字符串
        else {
            this.setLoginStr(str);
        }
        // 返回自身以便链式赋值
        return this;
    }

    public String toString() {
        // 某个微信公众号
        if (isByWeixinGhOpenId())
            return "wxgh:" + weixinPNB + ":" + weixinOpenId;

        // OAuth 认证
        if (isByOAuth())
            return "oauth:" + oauthProvider + ":" + oauthProfileId;

        // 采用登录字符串
        return loginStr;
    }

    public WnQuery joinQuery(WnQuery q) {
        // 某个微信公众号
        if (isByWeixinGhOpenId())
            return q.setv("wxgh_" + this.weixinPNB, this.weixinOpenId);

        // OAuth 认证
        if (isByOAuth())
            return q.setv("oauth_" + this.oauthProvider, this.oauthProfileId);

        // 手机
        if (isByPhone())
            return q.setv("phone", this.phone);

        // 邮箱
        if (this.isByEmail())
            return q.setv("email", this.email);

        // 登录名
        if (this.isByName())
            return q.setv("nm", this.name);

        // 考不科能
        throw Lang.impossible();
    }

    public WnObj joinObj(WnObj o) {
        // 某个微信公众号
        if (isByWeixinGhOpenId()) {
            o.setv("wxgh_" + this.weixinPNB, this.weixinOpenId);
            return o;
        }

        // OAuth 认证
        if (isByOAuth()) {
            o.setv("oauth_" + this.oauthProvider, this.oauthProfileId);
            return o;
        }

        // 手机
        if (isByPhone()) {
            o.setv("phone", this.phone);
            return o;
        }

        // 邮箱
        if (this.isByEmail()) {
            o.setv("email", this.email);
            return o;
        }

        // 登录名
        if (this.isByName()) {
            o.setv("nm", this.name);
            return o;
        }

        // 考不科能
        throw Lang.impossible();
    }

    public String getLoginStr() {
        return loginStr;
    }

    public void setLoginStr(String loginStr) {
        if (null == loginStr)
            throw Er.create("e.usr.loginstr.null");

        // 首先整理一下字符串，去掉所有的空格
        String str = loginStr.replaceAll("[ \t\r\n]", "");

        if (str.length() == 0)
            throw Er.create("e.usr.loginstr.empty");

        if (str.length() < 4)
            throw Er.create("e.usr.loginstr.tooshort");

        // 手机
        if (str.matches("^[0-9+-]{11,20}$")) {
            phone = str;
        }
        // 邮箱
        else if (str.matches("^[0-9a-zA-Z_.-]+@[0-9a-zA-Z_.-]+.[0-9a-zA-Z_.-]+$")) {
            email = str;
        }
        // 登录名
        else if (str.matches("^[0-9a-zA-Z._-]{4,}$")) {
            name = str;
        }
        // 错误的登录字符串
        else {
            throw Er.create("e.usr.loginstr.invalid", loginStr);
        }

        // 记录原始字符串
        this.loginStr = loginStr;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLoginPassword() {
        return loginPassword;
    }

    public void setLoginPassword(String loginPassword) {
        this.loginPassword = loginPassword;
    }

    public String getOauthProvider() {
        return oauthProvider;
    }

    public void setOauthProvider(String oauthProvider) {
        this.oauthProvider = oauthProvider;
    }

    public String getOauthProfileId() {
        return oauthProfileId;
    }

    public void setOauthProfileId(String oauthProfileId) {
        this.oauthProfileId = oauthProfileId;
    }

    public String getWeixinPNB() {
        return weixinPNB;
    }

    public void setWeixinPNB(String weixinPNB) {
        this.weixinPNB = weixinPNB;
    }

    public String getWeixinOpenId() {
        return weixinOpenId;
    }

    public void setWeixinOpenId(String weixinOpenId) {
        this.weixinOpenId = weixinOpenId;
    }

    public boolean isByLoginStr() {
        return !Strings.isBlank(loginStr);
    }

    public boolean isByPhone() {
        return !Strings.isBlank(phone);
    }

    public boolean isByEmail() {
        return !Strings.isBlank(email);
    }

    public boolean isByName() {
        return !Strings.isBlank(name);
    }

    public boolean isByOAuth() {
        return !Strings.isBlank(oauthProvider);
    }

    public boolean isByWeixinGhOpenId() {
        return !Strings.isBlank(weixinOpenId);
    }

}
