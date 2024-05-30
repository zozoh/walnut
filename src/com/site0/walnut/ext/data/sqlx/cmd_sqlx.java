package com.site0.walnut.ext.data.sqlx;

import java.util.HashMap;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.sqlx.loader.SqlHolder;
import com.site0.walnut.ext.data.sqlx.loader.WnSqlHolder;
import com.site0.walnut.ext.sys.sql.WnDaos;
import com.site0.walnut.impl.box.JvmFilterExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.ZParams;

public class cmd_sqlx extends JvmFilterExecutor<SqlxContext, SqlxFilter> {

    static Map<String, SqlHolder> sqlHolders = new HashMap<>();

    static SqlHolder getSqlHolder(WnIo io, WnObj oDir) {
        String key = oDir.id();
        SqlHolder sqls = sqlHolders.get(key);
        if (null == sqls) {
            synchronized (cmd_sqlx.class) {
                sqls = sqlHolders.get(key);
                if (null == sqls) {
                    sqls = new WnSqlHolder(io, oDir);
                    sqlHolders.put(key, sqls);
                }
            }
        }
        return sqls;
    }

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
        String daoName = fc.params.val(0, "default");
        fc.auth = WnDaos.loadAuth(sys, daoName);

        String dirPath = fc.params.getString("conf", "~/.sqlx");
        WnObj oDir = Wn.checkObj(sys, dirPath);
        fc.sqls = getSqlHolder(sys.io, oDir);

        // 读取输入
        String json = sys.in.readAll();
        NutMap input = Json.fromJson(NutMap.class, json);
        fc.setInput(input);

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
