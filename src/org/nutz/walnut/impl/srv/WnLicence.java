package org.nutz.walnut.impl.srv;

import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;

public class WnLicence {

    private String name;

    private String signDate;

    private String belongTo;

    private String verify;

    private NutMap privilege;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSignDate() {
        return signDate;
    }

    public void setSignDate(String signDate) {
        this.signDate = signDate;
    }

    public String getBelongTo() {
        return belongTo;
    }

    public void setBelongTo(String belongTo) {
        this.belongTo = belongTo;
    }

    public String getVerify() {
        return verify;
    }

    public void setVerify(String verify) {
        this.verify = verify;
    }

    public boolean hasVerify() {
        return !Strings.isBlank(this.verify);
    }

    public NutMap getPrivilege() {
        return privilege;
    }

    public void setPrivilege(NutMap privilege) {
        this.privilege = privilege;
    }

    public boolean hasPrivilege() {
        return null != this.privilege;
    }

}
