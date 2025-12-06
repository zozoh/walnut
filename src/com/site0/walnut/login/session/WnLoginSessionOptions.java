package com.site0.walnut.login.session;

import org.nutz.lang.util.NutMap;

/**
 * 本结构可以用来创建<code>WnSqlSessionStore</code>或<code>WnStdSessionStore</code>
 * 判断的逻辑如下:
 * 
 * <ol>
 * <li>如果声明了 sqlFetch ，采用<code>WnSqlSessionStore</code>
 * <li>如果声明了 path ，采用<code>WnStdSessionStore</code>
 * <li>其他情况则会抛错
 * </ol>
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnLoginSessionOptions {

    /**
     * 会话信息存储的路径，譬如 "~/.domain/session"
     */
    public String path;

    /**
     * 数据源名称，默认 default
     */
    public String daoName;

    /**
     * SQL 模板文件存储目录，默认 "~/.sqlx"
     */
    public String sqlHome;
    
    /**
     * 获取会话记录列表的 SQL，需要支持变量
     * 
     * <ul>
     * <li><code>filter</code>
     * <li><code>sorter</code> 
     * <li><code>skip</code> 
     * <li><code>limit</code> 
     * </ul>
     */
    public String sqlQuery;

    /**
     * 获取会话记录的 SQL，需要支持变量
     * 
     * <ul>
     * <li><code>id</code> 会话的 ID 或者说 Ticket
     * </ul>
     */
    public String sqlFetch;

    /**
     * 删除会话记录的 SQL，需要支持变量
     * 
     * <ul>
     * <li><code>id</code> 会话的 ID 或者说 Ticket
     * <li><code>type</code> 会话类型
     * <li><code>u_name</code> 会话用户登录名
     * <li><code>u_id</code> 会话用户ID
     * </ul>
     * 
     * SQL 假设，调用者会给出 id, 或 (type+u_name) 或者 (type+u_id)
     * 这样的组合，如果条件为空，SQL 语句应该出错
     */
    public String sqlDelete;

    /**
     * 更新会话记录的 SQL，数据表需要支持字段：
     * 
     * <ul>
     * <li><code>id</code> 【条件】会话的 ID 或者说 Ticket
     * <li><code>expi_at</code> 【字段】过期时间
     * <li><code>env</code> 【字段】会话环境变量
     * </ul>
     * 
     * 上述字段除了 id，数据表里如果没有，那么对应的 SQL 模板里应该过滤掉对应的输入变量
     */
    public String sqlUpdate;

    /**
     * 创建会话记录的 SQL，数据表需要支持字段：
     * 
     * <ul>
     * <li><code>id</code> 【条件】会话的 ID 或者说 Ticket
     * <li><code>expi_at</code> 【字段】过期时间
     * <li><code>u_id</code> 【字段】关联用户 ID
     * <li><code>u_name</code> 【字段】用户登录名
     * <li><code>email</code> 【字段】用户邮箱
     * <li><code>phone</code> 【字段】用户手机号
     * <li><code>env</code> 【字段】会话环境变量
     * </ul>
     * 
     * 上述字段除了 id，数据表里如果没有，那么对应的 SQL 模板里应该过滤掉对应的输入变量
     */
    public String sqlInsert;

    public NutMap defaultEnv;

}
