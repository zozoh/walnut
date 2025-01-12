package com.site0.walnut.ext.data.sqlx;

import java.sql.SQLException;
import java.util.List;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.ext.sys.sql.WnDaos;
import com.site0.walnut.impl.box.JvmFilterExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.ZParams;
import com.site0.walnut.ext.data.sqlx.util.Sqlx;

public class cmd_sqlx extends JvmFilterExecutor<SqlxContext, SqlxFilter> {

    private static final Log log = Wlog.getCMD();

    public cmd_sqlx() {
        super(SqlxContext.class, SqlxFilter.class);
    }

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqnl");
    }

    @Override
    protected SqlxContext newContext() {
        return new SqlxContext();
    }

    @Override
    protected void onFinished(WnSystem sys, SqlxContext fc) {
        fc.closeConnection();
    }

    @Override
    protected void prepare(WnSystem sys, SqlxContext fc) {
        fc.setup(sys);

        String daoName = fc.params.val(0, "default");
        fc.auth = WnDaos.loadAuth(sys, daoName);

        String dirPath = fc.params.getString("conf", "~/.sqlx");
        fc.sqls = Sqlx.getSqlHolderByPath(sys, dirPath);

        if (log.isDebugEnabled()) {
            log.debugf("sqlx prepare: %s : %s", daoName, dirPath);
        }

        // 读取输入
        String json = sys.in.readAll();
        NutMap input = Json.fromJson(NutMap.class, json);
        fc.setInput(input);

    }

    @Override
    protected void _exec_filters(WnSystem sys,
                                 List<SqlxFilter> hdlFilters,
                                 List<ZParams> hdlParams,
                                 SqlxContext fc) {
        try {
            super._exec_filters(sys, hdlFilters, hdlParams, fc);
        }
        // 遇到错误就回滚
        catch (Throwable e) {
            if (fc.hasTransLevel()) {
                log.errorf("Rollback for Error: %s", e.toString());
                try {
                    fc.rollback();
                }
                catch (SQLException e1) {
                    log.errorf("!!Rollback Fail: %s", e1.toString());
                    throw Er.wrap(e1);
                }
            }
            throw Er.wrap(e);
        }
    }

    @Override
    protected void output(WnSystem sys, SqlxContext fc) {
        // 子命令阻止输出
        if (fc.quiet) {
            return;
        }
        // 输出
        JsonFormat jfmt = Cmds.gen_json_format(fc.params);
        String str = Json.toJson(fc.result, jfmt);
        sys.out.println(str);
    }

}
