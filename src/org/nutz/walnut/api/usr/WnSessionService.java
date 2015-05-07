package org.nutz.walnut.api.usr;

import org.nutz.lang.util.NutMap;

public interface WnSessionService {

    /**
     * 创建一个新的会话
     * 
     * @param nm
     *            用户名
     * @param pwd
     *            密码
     * @return 会话对象
     * @throws <ul>
     *         <li>"e.usr.noexists"
     *         <li>"e.usr.blank.pwd"
     *         <li>"e.usr.invalid.login"
     *         </ul>
     */
    WnSession login(String nm, String pwd);

    /**
     * 为某用户直接创建一个 Session
     * 
     * @param u
     *            用户
     * @return 会话对象
     */
    WnSession create(WnUsr u);

    WnSession logout(String seid);

    void setEnvs(WnSession se, NutMap map);

    void setEnv(WnSession se, String nm, String val);

    NutMap removeEnv(String seid, String... nms);

    void touch(String seid);

    /**
     * 根据 ID 获取一个会话对象
     * 
     * @param seid
     *            会话的 ID
     * @return 会话对象
     */
    WnSession fetch(String seid);

    /**
     * 根据 ID 获取一个会话对象。如果会话对象不存在或者过期，则统统抛错
     * 
     * @param seid
     *            会话的 ID
     * @return 会话对象
     * @throws <ul>
     *         <li>"e.session.noexists"
     *         <li>"e.session.expired"
     *         </ul>
     */
    WnSession check(String seid);

}