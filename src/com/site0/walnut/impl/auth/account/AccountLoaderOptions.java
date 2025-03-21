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
    public String setup;
    public NutBean dftEnv;
}
