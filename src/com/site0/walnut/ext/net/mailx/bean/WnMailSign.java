package com.site0.walnut.ext.net.mailx.bean;

import org.nutz.lang.util.NutBean;
import com.site0.walnut.util.tmpl.WnTmpl;

public class WnMailSign {

    private String storePath;

    private String storePassword;

    private String keyAlias;

    private String keyPassword;

    public void render(NutBean vars) {
        storePath = WnTmpl.exec(storePath, vars);
        storePassword = WnTmpl.exec(storePassword, vars);
        keyAlias = WnTmpl.exec(keyAlias, vars);
        keyPassword = WnTmpl.exec(keyPassword, vars);
    }

    public String getStorePath() {
        return storePath;
    }

    public void setStorePath(String storePath) {
        this.storePath = storePath;
    }

    public String getStorePassword() {
        return storePassword;
    }

    public void setStorePassword(String storePassword) {
        this.storePassword = storePassword;
    }

    public String getKeyAlias() {
        return keyAlias;
    }

    public void setKeyAlias(String keyAlias) {
        this.keyAlias = keyAlias;
    }

    public String getKeyPassword() {
        return keyPassword;
    }

    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }

}
