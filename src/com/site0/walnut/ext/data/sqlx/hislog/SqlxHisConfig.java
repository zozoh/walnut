package com.site0.walnut.ext.data.sqlx.hislog;

import org.nutz.lang.util.NutMap;

public class SqlxHisConfig {

    private NutMap assign;

    private HisConfigItem[] logs;

    private SqlxHisTarget[] target;

    public NutMap getAssign() {
        return assign;
    }

    public void setAssign(NutMap assign) {
        this.assign = assign;
    }

    public boolean hasValidLogs() {
        if (null == logs || logs.length == 0) {
            return false;
        }
        for (HisConfigItem it : logs) {
            if (it.isValid()) {
                return true;
            }
        }
        return false;
    }

    public HisConfigItem[] getLogs() {
        return logs;
    }

    public void setLogs(HisConfigItem[] logs) {
        this.logs = logs;
    }

    public boolean hasValidTarget() {
        if (null == target || target.length == 0) {
            return false;
        }
        for (SqlxHisTarget ta : target) {
            if (ta.isValid()) {
                return true;
            }
        }
        return false;
    }

    public SqlxHisTarget[] getTarget() {
        return target;
    }

    public void setTarget(SqlxHisTarget[] target) {
        this.target = target;
    }

}
