package org.nutz.walnut.impl.usr;

import org.nutz.json.JsonField;
import org.nutz.json.JsonFormat;
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

    /**
     * 存放需要持久化的变量
     */
    private NutMap envs;

    /**
     * 存放临时的变量
     */
    @JsonField(ignore = true)
    private NutMap tmp_vars;

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
    public NutMap vars() {
        if (null == tmp_vars) {
            synchronized (this) {
                if (null == tmp_vars) {
                    tmp_vars = new NutMap().attach(envs);
                }
            }
        }
        return tmp_vars;
    }

    @Override
    public WnSession setEnvs(NutMap envs) {
        this.envs = envs;

        if (null == this.tmp_vars)
            this.tmp_vars = new NutMap();

        this.tmp_vars.attach(this.envs);

        return this;
    }

    public void setTmp_vars(NutMap tmp_vars) {
        if (null == this.tmp_vars) {
            this.tmp_vars = new NutMap();
        }
        if (null != this.envs) {
            this.tmp_vars.attach(this.envs);
        }
    }

    @Override
    public WnSession var(String nm, Object val) {
        vars().setv(nm, val);
        return this;
    }

    @Override
    public Object var(String nm) {
        return vars().get(nm);
    }

    @Override
    public void persist(String... nms) {
        for (String nm : nms) {
            Object val = this.tmp_vars.remove(nm);
            this.envs.setOrRemove(nm, val);
        }
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
        this.grp = me.mainGroup();
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
        se.tmp_vars = new NutMap().attach(se.envs);
        if (null != tmp_vars)
            se.tmp_vars.putAll(tmp_vars);
        return se;
    }

    public NutMap toMapForClient(JsonFormat fmt) {
        NutMap map = new NutMap();
        map.put("id", this.id);
        map.put("me", this.me);
        map.put("grp", this.grp);

        NutMap myEnvs = new NutMap();
        myEnvs.putAll(this.envs);
        myEnvs.putAll(this.tmp_vars);
        map.put("envs", myEnvs);
        return map;
    }
}
