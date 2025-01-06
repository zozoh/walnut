package com.site0.walnut.ext.data.sqlx.hislog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.ext.data.sqlx.SqlxContext;
import com.site0.walnut.util.explain.WnExplain;
import com.site0.walnut.util.explain.WnExplains;

public class SqlxHislogRuntime {

    SqlxHislogConfig config;

    private SqlxContext fc;

    private WnExplain globalAssign;

    private List<SqlxHislogRuntimeItem> logs;

    public SqlxHislogRuntime(SqlxHislogConfig config, SqlxContext fc) {
        this.config = config;
        this.fc = fc;
        this.prepare();
    }

    private void prepare() {
        if (null != config.getAssign()) {
            globalAssign = WnExplains.parse(config.getAssign());
        }
        if (null != config.getLogs()) {
            this.logs = new ArrayList<>(config.getLogs().length);
            for (SqlxHislogConfigItem confItem : config.getLogs()) {
                SqlxHislogRuntimeItem it = new SqlxHislogRuntimeItem(confItem);
                this.logs.add(it);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public NutBean createGlobalContext() {
        NutBean g = fc.getMergedInputAndPipeContext();
        if (null != globalAssign) {
            Object re = globalAssign.explain(g);
            return NutMap.WRAP((Map<String, Object>) re);
        }
        return new NutMap();
    }

}
