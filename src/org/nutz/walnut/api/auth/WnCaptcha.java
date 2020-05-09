package org.nutz.walnut.api.auth;

import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;

public class WnCaptcha {

    private String scene;

    private String account;

    private String code;

    private int retry;

    private int maxRetry;

    private long expi;

    /**
     * 持续时间（分钟）
     */
    private int duInMin;

    public WnCaptcha(String scene, String account) {
        this.scene = scene;
        this.account = account;
        this.retry = 0;
        this.maxRetry = 3;
        // 默认过期时间 10 分钟
        this.expi = System.currentTimeMillis() + 600000L;
    }

    public WnCaptcha(WnObj oCa) {
        scene = oCa.getString("scene");
        account = oCa.name();
        code = oCa.getString("code");
        retry = oCa.getInt("retry", 0);
        maxRetry = oCa.getInt("remax", 3);
        expi = oCa.expireTime();
        duInMin = oCa.getInt("du_in_min");
    }

    public NutMap toMeta(String accountKey) {
        NutMap map = new NutMap();
        map.put("scene", scene);
        map.put("code", code);
        map.put("retry", retry);
        map.put("remax", maxRetry);
        map.put("expi", expi);
        map.put("du_in_min", duInMin);
        map.put("du_in_hr", duInMin / 60);

        if (!Strings.isBlank(accountKey)) {
            map.put(accountKey, account);
        }
        return map;
    }

    public String getRetryKey() {
        return "retry";
    }

    public String getScene() {
        return scene;
    }

    public void setScene(String scene) {
        this.scene = scene;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isSameCode(String code) {
        return this.code.equals(code);
    }

    public int getRetry() {
        return retry;
    }

    public void setRetry(int retry) {
        this.retry = retry;
    }

    public void incRetry() {
        this.retry++;
    }

    public int getMaxRetry() {
        return maxRetry;
    }

    public void setMaxRetry(int maxRetry) {
        this.maxRetry = maxRetry;
    }

    public boolean isNoMoreRetry() {
        return this.retry > this.maxRetry;
    }

    public long getExpi() {
        return expi;
    }

    public void setExpi(long expi) {
        this.expi = expi;
    }

    public void setExpiFromNowByMin(int duInMin) {
        this.expi = System.currentTimeMillis() + duInMin * 60000L;
        this.duInMin = duInMin;
    }

    public boolean isExpired() {
        return this.expi <= System.currentTimeMillis();
    }

}
