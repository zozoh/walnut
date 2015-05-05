package org.nutz.walnut.impl.usr;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.usr.WnSession;
import org.nutz.walnut.api.usr.WnUsr;

/**
 * 记录一个 ZSession 相关信息
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class IoWnSession implements WnSession {

    private String id;

    private NutMap envs;

    private String me;

    private String grp;

    /**
     * @return 会话ID
     */
    @Override
    public String id() {
        return id;
    }

    /**
     * 设置会话 ID
     * 
     * @param id
     *            会话ID
     * @return 自身以便链式赋值
     */
    @Override
    public WnSession id(String id) {
        this.id = id;
        return this;
    }

    @Override
    public NutMap envs() {
        return envs;
    }

    @Override
    public void envs(NutMap envs) {
        this.envs = envs;
    }

    @Override
    public WnSession env(String nm, Object val) {
        if (null == envs) {
            envs = new NutMap();
        }
        envs.setv(nm, val);
        return this;
    }

    @Override
    public String me() {
        return me;
    }

    @Override
    public String group() {
        return grp;
    }

    @Override
    public WnSession me(WnUsr me) {
        this.me = me.name();
        this.grp = me.group();
        return this;
    }

    @Override
    public WnSession clone() {
        IoWnSession se = new IoWnSession();
        se.id = id;
        se.me = me;
        se.grp = grp;
        se.envs = new NutMap();
        se.envs.putAll(envs);
        return se;
    }

}
