package org.nutz.walnut.api.usr;

import org.nutz.lang.util.NutMap;

public interface WnSession {

    /**
     * @return 会话ID
     */
    String id();

    /**
     * 设置会话 ID
     * 
     * @param id
     *            会话ID
     * @return 自身以便链式赋值
     */
    WnSession id(String id);

    NutMap envs();

    WnSession envs(NutMap envs);

    WnSession env(String nm, Object val);

    String me();

    String group();

    WnSession me(WnUsr me);

    WnSession clone();

}