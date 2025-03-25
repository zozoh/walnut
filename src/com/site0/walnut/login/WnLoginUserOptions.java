package com.site0.walnut.login;

import org.nutz.lang.util.NutMap;

/**
 * 本结构可以用来创建<code>WnSqlUserStore</code>或<code>WnStdUserStore</code> 判断的逻辑如下:
 * 
 * <ol>
 * <li>如果声明了 sqlFetch ，采用<code>WnSqlUserStore</code>
 * <li>如果声明了 path ，采用<code>WnStdUserStore</code>
 * <li>其他情况则会抛错
 * </ol>
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnLoginUserOptions {

    /**
     * 用户信息存储的路径，譬如 "~/user"
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
     * 获取用户记录的 SQL，需要支持变量
     * 
     * <ul>
     * <li><code>id</code> 用户的 ID 或者说 Ticket
     * </ul>
     */
    public String sqlFetch;

    /**
     * 获取用户记录的 SQL，需要支持变量
     * 
     * <ul>
     * <li><code>filter</code> 用户表的过滤条件
     * <li><code>sorter</code> 查询的排序方式
     * <li><code>limit</code> 最大查询数量
     * </ul>
     */
    public String sqlQuery;

    /**
     * 更新用户记录的 SQL，数据表需要支持字段：
     * 
     * <ul>
     * <li><code>id</code> 【条件】用户的 ID 或者说 Ticket
     * <li><code>nm</code> 【字段】用户登录名
     * <li><code>phone</code> 【字段】手机号
     * <li><code>email</code> 【字段】邮箱
     * <li><code>passwd</code> 【字段】加盐后密码
     * <li><code>salt</code> 【字段】盐值
     * <li><code>last_login_at</code> 【字段】最后登录时间
     * <li><code>...</code> 【字段】其他自定义字段
     * </ul>
     * 
     * 上述字段除了 id，数据表里如果没有，那么对应的 SQL 模板里应该过滤掉对应的输入变量
     */
    public String sqlUpdate;

    /**
     * 创建用户记录的 SQL，数据表需要支持字段：
     * 
     * <ul>
     * <li><code>id</code> 【条件】用户的 ID 或者说 Ticket
     * <li><code>nm</code> 【字段】用户登录名
     * <li><code>phone</code> 【字段】手机号
     * <li><code>email</code> 【字段】邮箱
     * <li><code>passwd</code> 【字段】加盐后密码
     * <li><code>salt</code> 【字段】盐值
     * <li><code>last_login_at</code> 【字段】最后登录时间
     * <li><code>...</code> 【字段】其他自定义字段
     * </ul>
     * 
     * 上述字段除了 id，数据表里如果没有，那么对应的 SQL 模板里应该过滤掉对应的输入变量
     */
    public String sqlInsert;
    
    public NutMap defaultMeta;

}
