package com.site0.walnut.ext.data.sqlx.hislog;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.explain.WnExplain;
import com.site0.walnut.util.explain.WnExplains;
import com.site0.walnut.util.validate.WnMatch;
import com.site0.walnut.util.validate.impl.AutoMatch;
import com.site0.walnut.val.ValueMakers;

public class HisRuntimeItem {

    private Pattern sqlName;

    private WnMatch test;

    private WnExplain data;

    private String toPipeKey;

    private HisRuntimeSetData[] setData;

    public HisRuntimeItem(WnSystem sys, HisConfigItem conf) {
        this.sqlName = Pattern.compile(conf.getSqlName());
        this.test = null;
        if (null != conf.getTest()) {
            this.test = AutoMatch.parse(conf.getTest());
        }
        this.data = WnExplains.parse(conf.getData());
        this.toPipeKey = conf.getTo();
        if (conf.hasSetData()) {
            HisConfigSetData[] sds = conf.getSetData();
            this.setData = new HisRuntimeSetData[sds.length];
            for (int i = 0; i < sds.length; i++) {
                HisConfigSetData sd = sds[i];
                HisRuntimeSetData rtSD = new HisRuntimeSetData();
                rtSD.asDefault = sd.isAsDefault();
                rtSD.name = sd.getName();
                rtSD.valueMaker = ValueMakers.build(sys, sd.getValue());
                this.setData[i] = rtSD;
            }
        }
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

    public NutMap createLogRecord(Date now, NutBean myContext, NutBean record) {
        myContext.put("item", record);
        Object re = this.data.explain(myContext);
        NutMap bean = NutMap.WrapAny(re);
        if (null != this.setData) {
            for (HisRuntimeSetData sd : this.setData) {
                // 仅设置默认值
                if (sd.asDefault && bean.has(sd.name)) {
                    continue;
                }
                // 设置值
                Object val = sd.valueMaker.make(now, myContext);
                bean.put(sd.name, val);
            }
        }
        return bean;

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
