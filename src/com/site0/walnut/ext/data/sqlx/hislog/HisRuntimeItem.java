package com.site0.walnut.ext.data.sqlx.hislog;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.util.Ws;
import com.site0.walnut.util.explain.WnExplain;
import com.site0.walnut.util.explain.WnExplains;
import com.site0.walnut.util.validate.WnMatch;
import com.site0.walnut.util.validate.impl.AutoMatch;

public class SqlxHislogRuntimeItem {

    private Pattern sqlName;

    private WnMatch test;

    private WnExplain data;

    private String toPipeKey;

    public SqlxHislogRuntimeItem(SqlxHislogConfigItem conf) {
        this.sqlName = Pattern.compile(conf.getSqlName());
        this.test = null;
        if (null != conf.getTest()) {
            this.test = AutoMatch.parse(conf.getTest());
        }
        this.data = WnExplains.parse(conf.getData());
        this.toPipeKey = conf.getTo();
    }

    public boolean trySqlName(String name, NutBean myContext) {
        Matcher m = this.sqlName.matcher(name);
        // 计入上下文
        if (m.find()) {
            myContext.put("sqlName", name);
            int n = m.groupCount();
            for (int i = 1; i <= n; i++) {
                myContext.put("sqlName" + i, m.group(i));
            }
            return true;
        }
        return false;
    }

    public boolean isMatchRecord(NutBean record) {
        if (null == test) {
            return true;
        }
        return test.match(record);
    }

    @SuppressWarnings("unchecked")
    public NutMap createLogRecord(NutBean myContext, NutBean record) {
        myContext.put("item", record);
        Object re = this.data.explain(myContext);
        return NutMap.WRAP((Map<String, Object>) re);

    }

    public Pattern getSqlName() {
        return sqlName;
    }

    public void setSqlName(Pattern sqlName) {
        this.sqlName = sqlName;
    }

    public WnMatch getTest() {
        return test;
    }

    public void setTest(WnMatch test) {
        this.test = test;
    }

    public WnExplain getData() {
        return data;
    }

    public void setData(WnExplain data) {
        this.data = data;
    }

    public boolean hasToPipeKey() {
        return !Ws.isBlank(toPipeKey);
    }

    public String getToPipeKey() {
        return toPipeKey;
    }

    public void setToPipeKey(String toPipeKey) {
        this.toPipeKey = toPipeKey;
    }

}
