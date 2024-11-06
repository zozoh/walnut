package com.site0.walnut.ext.data.sqlx.hdl;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.ext.data.sqlx.SqlxContext;
import com.site0.walnut.ext.data.sqlx.SqlxFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class sqlx_trans extends SqlxFilter {

    @Override
    protected void process(WnSystem sys, SqlxContext fc, ZParams params) {
        // 分析参数
        boolean keepAlive = params.is("keep");
        if (keepAlive) {
            // 暂时不支持 -keep 模式
            throw Er.create("e.cmd.sqlx.trans.KeepAliveNotSupport");
        }
        int tl = params.getInt("level", -1);

        // 设置事务级别
        fc.setTransLevel(tl);

    }

}
