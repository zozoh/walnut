package org.nutz.walnut.api.usr;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.impl.usr.IoWnUsr;

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
     * @param reuse
     *            是否重用当前会话。即当前会话如果与用户重名，直接重用
     * @return 会话对象
     */
    WnSession create(IoWnUsr u, boolean reuse);

    /**
     * 销毁一个会话对象，如果会话对象有父，则将父会话的 lm 更新
     * 
     * @param seid
     *            会话对象ID
     * 
     * @return null 表示当前用户没有 session 了，否则返回父 session 对象
     */
    WnSession logout(String seid);

    NutMap setEnvs(String seid, NutMap map);

    NutMap setEnv(String seid, String nm, String val);

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