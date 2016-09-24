package org.nutz.walnut.api.usr;

/**
 * 会话服务接口
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public interface WnSessionService {

    WnUsrService usrs();

    /**
     * 创建一个新的会话
     * 
     * @param nm
     *            用户名
     * @param pwd
     *            密码
     * @param du
     *            会话持续时间，小于0 表示默认
     * @return 会话对象
     * @throws
     *             <ul>
     *             <li>"e.usr.noexists"
     *             <li>"e.usr.blank.pwd"
     *             <li>"e.usr.invalid.login"
     *             </ul>
     */
    WnSession login(String nm, String pwd, long du);

    /**
     * @see #login(String, String, long)
     */
    WnSession login(String nm, String pwd);

    /**
     * @param pse
     *            父会话
     * @param u
     *            用户
     * @param du
     *            会话持续时间，小于0 表示默认
     * 
     * @return 会话对象
     */
    WnSession create(WnSession pse, WnUsr u, long du);

    /**
     * @see #create(WnSession, WnUsr, long)
     */
    WnSession create(WnSession pse, WnUsr u);

    /**
     * 为某用户直接创建一个 Session
     * 
     * @param u
     *            用户
     * @param du
     *            会话持续时间，小于0 表示默认
     * @return 会话对象
     */
    WnSession create(WnUsr u, long du);

    /**
     * @see #create(WnUsr, long)
     */
    WnSession create(WnUsr u);

    /**
     * 删除会话
     * 
     * @param seid
     *            会话 ID
     * @return 父会话，null 表示没有父会话
     */
    WnSession logout(String seid);

    /**
     * 更新会话。会同时更新所有的父会话
     * 
     * @param seid
     *            会话 ID
     */
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
     * @param autoTouch
     *            如果取得了会话，是否自动更新会话过期时间
     * @return 会话对象
     * @throws
     *             <ul>
     *             <li>"e.se.noexists"
     *             <li>"e.se.expired"
     *             </ul>
     */
    WnSession check(String seid, boolean autoTouch);

}