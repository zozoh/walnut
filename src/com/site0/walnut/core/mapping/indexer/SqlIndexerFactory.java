package com.site0.walnut.core.mapping.indexer;

import org.nutz.lang.util.NutBean;

import com.site0.walnut.api.io.MimeMap;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnIoIndexer;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.core.indexer.sql.SqlIndexer;
import com.site0.walnut.core.mapping.WnIndexerFactory;
import com.site0.walnut.core.mapping.support.SqlIoArgs;
import com.site0.walnut.ext.data.sqlx.loader.SqlHolder;
import com.site0.walnut.ext.data.sqlx.util.Sqlx;
import com.site0.walnut.ext.sys.sql.WnDaoAuth;
import com.site0.walnut.ext.sys.sql.WnDaos;
import com.site0.walnut.util.Wn;

public class SqlIndexerFactory implements WnIndexerFactory {

    /**
     * 这个需要通过 IOC 注入得到实例
     */
    private WnIo io;
    private MimeMap mimes;

    public void setIo(WnIo io) {
        this.io = io;
    }

    public void setMimes(MimeMap mimes) {
        this.mimes = mimes;
    }

    @Override
    public WnIoIndexer load(WnObj oHome, String args) {
        SqlIoArgs _args = new SqlIoArgs(args);

        // 读取数据源的配置信息
        NutBean vars = Wn.getVarsByObj(oHome);
        WnDaoAuth auth = WnDaos.loadAuth(io, _args.daoName, vars);

        // 获取 SQL 模板管理器
        SqlHolder sqls = Sqlx.getSqlHolderByPath(io, vars, null);

        return new SqlIndexer(oHome, mimes, auth, sqls, _args);
    }

}
