package com.site0.walnut.impl.srv;

import org.nutz.lang.util.NutMap;

public class WnAppLicenceInfo {

    public static class AutoGenCode {

        private int day;

        private String licence;

        public int getDay() {
            return day;
        }

        public void setDay(int day) {
            this.day = day;
        }

        public String getLicence() {
            return licence;
        }

        public void setLicence(String licence) {
            this.licence = licence;
        }

    }

    private String appName;

    private String provider;

    private AutoGenCode autoGenCode;

    private String errMessage;

    public boolean isCanAutoGenCode() {
        return null != autoGenCode;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public AutoGenCode getAutoGenCode() {
        return autoGenCode;
    }

    public void setAutoGenCode(AutoGenCode autoGenCode) {
        this.autoGenCode = autoGenCode;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public void setErrMessage(String errMessage) {
        this.errMessage = errMessage;
    }

    /**
     * @return 给界面对象渲染用的信息
     */
    public NutMap toContextMap() {
        NutMap map = new NutMap();
        map.put("appName", appName);
        map.put("provider", provider);
        map.put("autoGencode", this.isCanAutoGenCode());
        map.put("errMessage", errMessage);
        return map;
    }

}
