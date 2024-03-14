package com.site0.walnut.ext.net.mailx.bean;

import org.nutz.lang.util.NutBean;
import com.site0.walnut.util.tmpl.WnTmpl;

public class WnMailSecurity {

    private WnMailSecuType type;

    private WnMailSign sign;

    private String encryptCertFile;

    public void render(NutBean vars) {
        if (null != sign) {
            sign.render(vars);
        }
        encryptCertFile = WnTmpl.exec(encryptCertFile, vars);
    }

    public boolean isSMIME() {
        return WnMailSecuType.SMIME == type;
    }

    public WnMailSecuType getType() {
        return type;
    }

    public void setType(WnMailSecuType type) {
        this.type = type;
    }

    public boolean hasSign() {
        return null != sign;
    }

    public WnMailSign getSign() {
        return sign;
    }

    public void setSign(WnMailSign sign) {
        this.sign = sign;
    }

    public boolean hasEncryptCertFile() {
        return null != encryptCertFile;
    }

    public String getEncryptCertFile() {
        return encryptCertFile;
    }

    public void setEncryptCertFile(String encryptCertFile) {
        this.encryptCertFile = encryptCertFile;
    }

}
