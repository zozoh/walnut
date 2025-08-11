package com.site0.walnut.ext.data.sqlx.hislog;

import org.nutz.lang.util.NutMap;

public class SqlxHislogConfig {
    
    private NutMap assign;
    
    private SqlxHislogConfigItem[] logs;

    public NutMap getAssign() {
        return assign;
    }

    public void setAssign(NutMap assign) {
        this.assign = assign;
    }

    public SqlxHislogConfigItem[] getLogs() {
        return logs;
    }

    public void setLogs(SqlxHislogConfigItem[] logs) {
        this.logs = logs;
    }

}
