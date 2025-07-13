package com.site0.walnut.login;

/**
 * 用户与角色的映射
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnLoginRoleOptions {

    /**
     * 角色信息存储的路径，譬如 "~/role"
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
     * 查询记录 SQL，需要支持变量
     * 
     * <ul>
     * <li><code>uid</code> 角色关联的用户ID
     * <li><code>grp</code> 角色关联的角色组名称
     * <li><code>usr</code> 【字段】角色关联的用户登录名
     * </ul>
     */
    public String sqlQuery;
    
    /**
     * 获取单条记录 SQL，需要支持变量
     * 
     * <ul>
     * <li><code>id</code> 角色ID
     * </ul>
     * 或者
     * <ul>
     * <li><code>uid</code> 角色关联的用户ID
     * <li><code>grp</code> 角色关联的角色组名称
     * </ul>
     */
    public String sqlFetch;

    /**
     * 删除记录的 SQL，需要支持变量
     * 
     * <ul>
     * <li><code>id</code> 角色的内部ID
     * </ul>
     */
    public String sqlDelete;

    /**
     * 创建记录的 SQL，数据表需要支持字段：
     * 
     * <ul>
     * <li><code>id</code> 角色的内部ID
     * <li><code>grp</code> 【字段】角色组名称
     * <li><code>uid</code> 【字段】角色关联的用户ID
     * <li><code>unm</code> 【字段】角色关联的用户登录名
     * <li><code>type</code> 【字段】角色类型
     * <li><code>role</code> 【字段】角色类型值
     * <li><code>ct</code> 【字段】创建时间
     * <li><code>lm</code> 【字段】修改时间
     * </ul>
     * 
     * 上述字段除了 <code>id</code>，数据表里如果没有，那么对应的 SQL 模板里应该过滤掉对应的输入变量
     */
    public String sqlInsert;
    
    /**
     * 更新记录的 SQL，数据表需要支持字段：
     * 
     * <ul>
     * <li><code>grp</code> 【字段】角色组名称 【更新条件】
     * <li><code>uid</code> 【字段】角色关联的用户ID 【更新条件】
     * <li><code>unm</code> 【字段】角色关联的用户登录名
     * <li><code>type</code> 【字段】角色类型
     * <li><code>role</code> 【字段】角色类型值
     * <li><code>ct</code> 【字段】创建时间
     * <li><code>lm</code> 【字段】修改时间
     * </ul>
     * 
     * 上述字段除了 <code>id</code>，数据表里如果没有，那么对应的 SQL 模板里应该过滤掉对应的输入变量
     */
    public String sqlUpdate;
}
