package org.nutz.walnut.api.usr;

import org.nutz.json.JsonFormat;
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

    WnSession setEnvs(NutMap envs);

    NutMap vars();

    WnSession var(String nm, Object val);

    Object var(String nm);

    void persist(String... nms);

    String me();

    String group();

    WnSession me(WnUsr me);

    WnSession clone();

    NutMap toMapForClient(JsonFormat fmt);
}