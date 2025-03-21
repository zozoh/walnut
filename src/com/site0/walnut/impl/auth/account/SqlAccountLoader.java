package com.site0.walnut.impl.auth.account;

import java.util.List;

import org.nutz.lang.util.NutBean;
import org.nutz.log.Log;

import com.site0.walnut.api.auth.WnAccount;
import com.site0.walnut.api.auth.WnAccountLoader;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.ext.data.sqlx.loader.SqlHolder;
import com.site0.walnut.ext.data.sqlx.processor.QueryProcessor;
import com.site0.walnut.ext.data.sqlx.util.Sqlx;
import com.site0.walnut.ext.sys.sql.WnDaoAuth;
import com.site0.walnut.ext.sys.sql.WnDaos;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.Ws;

public class SqlAccountLoader implements WnAccountLoader {

    private static Log log = Wlog.getAUTH();

    private QueryProcessor query;

    public WnDaoAuth auth;

    public SqlHolder sqls;

    public SqlAccountLoader(AccountLoaderOptions options) {
        WnIo io = options.io;
        NutBean sessionVars = options.sessionVars;

        // 分析 SQL
        String[] ss = options.setup.split(":");
        String daoName = Ws.sBlank(Ws.trim(ss[0]), "default");
        this.auth = WnDaos.loadAuth(io, daoName, sessionVars);

        // 准备 SQL 管理器
        this.sqls = Sqlx.getSqlHolderByPath(io, sessionVars, "~/.sqlx");

        // 构建查询器
        this.query = new QueryProcessor(log);
    }

    @Override
    public List<WnAccount> queryAccount(WnQuery q) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WnAccount getAccount(String nameOrPhoneOrEmail) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WnAccount checkAccount(String nameOrPhoneOrEmail) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WnAccount getAccount(WnAccount info) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WnAccount checkAccount(WnAccount info) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WnAccount getAccountById(String uid) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WnAccount checkAccountById(String uid) {
        // TODO Auto-generated method stub
        return null;
    }

}
