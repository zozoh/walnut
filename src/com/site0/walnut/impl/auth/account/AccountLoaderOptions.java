package com.site0.walnut.impl.auth.account;

import org.nutz.lang.util.NutBean;

import com.site0.walnut.api.io.WnIo;

public class AccountLoaderOptions {
    public WnIo io;
    public NutBean sessionVars;
    /**
     * 配置字符串，对于标准数据模型，就是账号目录的路径 对于 SQL 格式应该类
     * <code>{daoName}:{querySql}:{fetchSql}</code> 譬如
     * <code>::pet.select:pet.fetch</code>
     */
    /**
     * 配置字符串，对于标准数据模型，就是账号目录的路径, 譬如 
     * <pre>
     * ~/user
     * </pre>
     * 
     * 也可以指定指定一个 SQL 数据库表，格式
     * 
     * <code>{daoName}:{querySql}:{fetchSql?}</code>
     * 
     * <pre>
     * # 采用默认数据源，指定了 query
     * ::pet.select
     * 
     * # 采用默认数据源，指定了 query/fetch
     * ::pet.select:pet.fetch 
     * 
     * # 采用history数据源，指定了 query
     * :history:pet.select
     * 
     * # 采用history数据源，指定了 query/fetch
     * :history:pet.select:pet.select:pet.fetch
     * </pre>
     */
    public String setup;
    
    /**
     * 默认环境变量
     */
    public NutBean dftEnv;
}
