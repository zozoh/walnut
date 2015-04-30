package org.nutz.walnut.impl.usr;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.usr.WnUsr;

/**
 * 保存一个用户的所有信息
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
@SuppressWarnings("serial")
public class IoWnUsr extends NutMap implements WnUsr {

    @Override
    public String id() {
        return getString("id");
    }

    @Override
    public WnUsr id(String id) {
        this.setv("id", id);
        return this;
    }

    @Override
    public String name() {
        return getString("nm");
    }

    @Override
    public WnUsr name(String name) {
        this.setv("nm", name);
        return this;
    }

    @Override
    public String alias() {
        return getString("aa");
    }

    @Override
    public WnUsr alias(String alias) {
        this.setv("aa", alias);
        return this;
    }

    @Override
    public String password() {
        return getString("passwd");
    }

    @Override
    public WnUsr password(String passwd) {
        this.setv("passwd", passwd);
        return this;
    }

    @Override
    public String email() {
        return getString("email");
    }

    @Override
    public WnUsr email(String email) {
        this.setv("email", email);
        return this;
    }

    @Override
    public String phone() {
        return getString("phone");
    }

    @Override
    public WnUsr phone(String phone) {
        this.setv("phone", phone);
        return this;
    }

    @Override
    public String home() {
        return getString("home");
    }

    @Override
    public WnUsr home(String home) {
        this.setv("home", home);
        return this;
    }

    @Override
    public WnUsr clone() {
        IoWnUsr u = new IoWnUsr();
        u.putAll(this);
        return u;
    }
}
