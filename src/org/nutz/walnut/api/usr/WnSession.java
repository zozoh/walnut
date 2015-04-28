package org.nutz.walnut.api.usr;

import org.nutz.lang.util.NutMap;

/**
 * 记录一个 ZSession 相关信息
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnSession {

    private String id;

    private NutMap envs;

    private String me;

    /**
     * @return 会话ID
     */
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
    public WnSession id(String id) {
        this.id = id;
        return this;
    }

    public NutMap envs() {
        return envs;
    }

    public void envs(NutMap envs) {
        this.envs = envs;
    }

    public WnSession env(String nm, Object val) {
        if (null == envs) {
            envs = new NutMap();
        }
        envs.setv(nm, val);
        return this;
    }

    public String me() {
        return me;
    }

    public WnSession me(String me) {
        this.me = me;
        return this;
    }

}
