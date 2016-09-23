package org.nutz.walnut.util;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.usr.AbstractWnSession;
import org.nutz.walnut.api.usr.WnSession;

public class FakeWnSession extends AbstractWnSession {

    private String id;
    private NutMap vars;
    private String me;
    private String grp;
    private long du;

    public FakeWnSession() {
        vars = new NutMap();
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setMe(String me) {
        this.me = me;
    }

    public void setGrp(String grp) {
        this.grp = grp;
    }

    public void setDu(long du) {
        this.du = du;
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public String getParentSessionId() {
        return null;
    }

    @Override
    public NutMap vars() {
        return this.vars;
    }

    @Override
    public WnSession var(String nm, Object val) {
        vars().setOrRemove(nm, val);
        return this;
    }

    @Override
    public Object var(String nm) {
        return vars().get(nm);
    }

    @Override
    public void save() {}

    @Override
    public String me() {
        return me;
    }

    @Override
    public String group() {
        return grp;
    }

    @Override
    public long duration() {
        return du;
    }

    @Override
    public long expireTime() {
        return System.currentTimeMillis() + du;
    }

    @Override
    public WnSession clone() {
        FakeWnSession se = new FakeWnSession();
        se.id = this.id;
        se.grp = this.grp;
        se.me = this.me;
        se.du = this.du;
        se.vars.putAll(this.vars);
        return se;
    }

}
