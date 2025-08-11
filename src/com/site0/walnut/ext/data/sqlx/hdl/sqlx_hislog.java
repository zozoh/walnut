package com.site0.walnut.ext.data.sqlx.hdl;

import org.nutz.log.Log;

import com.site0.walnut.ext.data.sqlx.SqlxContext;
import com.site0.walnut.ext.data.sqlx.SqlxFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

public class sqlx_hislog extends SqlxFilter {

    private static final Log log = Wlog.getCMD();

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(off)$");
    }

    @Override
    protected void process(WnSystem sys, SqlxContext fc, ZParams params) {
        // 禁止了历史记录
        if (params.is("off")) {
            if (log.isDebugEnabled()) {
                log.debug("sqlx hislog off");
            }
            fc.hislog = null;
            return;
        }
        String logConfPath = params.getString("f");

        // 未设置 History，那么就不管，如果主命令初始化了 hislog，就用它
        if (Ws.isBlank(logConfPath)) {
            if (!fc.hasHislogRuntime()) {
                if (log.isDebugEnabled()) {
                    log.debug("sqlx hislog without defined, skip");
                }
            }
            return;
        }

        // 自己指定了
        fc.setHislogRuntime(sys, logConfPath);
    }

}
