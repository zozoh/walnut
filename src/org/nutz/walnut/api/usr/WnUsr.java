package org.nutz.walnut.api.usr;

import org.nutz.lang.util.NutMap;

/**
 * 保存一个用户的所有信息
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
@SuppressWarnings("serial")
public class WnUsr extends NutMap {

    /*
     * @return 当前用户是否是root, 也就是名字为root
     */
    // public boolean isRoot() {
    // return "root".equals(name());
    // }

    public String id() {
        return getString("id");
    }

    public WnUsr id(String id) {
        this.setv("id", id);
        return this;
    }

    public String name() {
        return getString("nm");
    }

    public WnUsr name(String name) {
        this.setv("nm", name);
        return this;
    }

    public String alias() {
        return getString("aa");
    }

    public WnUsr alias(String alias) {
        this.setv("aa", alias);
        return this;
    }

    public String password() {
        return getString("passwd");
    }

    public WnUsr password(String passwd) {
        this.setv("passwd", passwd);
        return this;
    }

    public String email() {
        return getString("email");
    }

    public WnUsr email(String email) {
        this.setv("email", email);
        return this;
    }

    public String phone() {
        return getString("phone");
    }

    public WnUsr phone(String phone) {
        this.setv("phone", phone);
        return this;
    }

    public String home() {
        return getString("home");
    }

    public WnUsr home(String home) {
        this.setv("home", home);
        return this;
    }

    public WnUsr clone() {
        WnUsr u = new WnUsr();
        u.putAll(this);
        return u;
    }
}
