package org.nutz.walnut.impl.usr;

import java.util.LinkedList;
import java.util.List;

import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.impl.io.WnBean;

/**
 * 保存一个用户的所有信息
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
@Deprecated
public class IoWnUsr extends WnBean implements WnUsr {

    @Override
    public boolean isNameSameAsId() {
        return this.isSameId(this.name());
    }

    @Override
    public String mainGroup() {
        return getString("grp");
    }

    @Override
    public WnUsr mainGroup(String grp) {
        this.setv("grp", grp);

        List<String> groups = this.myGroups();
        if (!groups.contains(grp)) {
            groups.add(grp);
        }

        return this;
    }

    @Override
    public List<String> myGroups() {
        List<String> groups = getList("my_grps", String.class);
        if (null == groups) {
            groups = new LinkedList<String>();
            this.myGroups(groups);
        }
        return groups;
    }

    @Override
    public WnUsr myGroups(List<String> groups) {
        this.setv("my_grps", groups);
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

    public WnUsr salt(String salt) {
        this.setv("salt", salt);
        return this;
    }

    public String salt() {
        return this.getString("salt");
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
    public String defaultObjPath() {
        return this.getString("DFT_OBJ_PATH");
    }

    @Override
    public WnUsr defaultObjPath(String dftObjPath) {
        this.setv("DFT_OBJ_PATH", dftObjPath);
        return this;
    }

    @Override
    public boolean hasDefaultObjPath() {
        return this.has("DFT_OBJ_PATH");
    }

    @Override
    public WnUsr clone() {
        IoWnUsr u = new IoWnUsr();
        u.update2(this);
        return u;
    }

    public String toString() {
        return String.format("%s(%s)", name(), id());
    }
}
