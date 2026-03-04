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

    private WnMatch testRecord;
    private WnMatch testContext;
    private WnMatch ignoreRecord;
    private WnMatch ignoreContext;

    private WnExplain data;

    private String toPipeKey;

    private HisRuntimeTrimData[] trimData;
    private HisRuntimeSetData[] setData;

    public HisRuntimeItem(WnSystem sys, HisConfigItem conf) {
        // 捕获
        this.sqlName = Pattern.compile(conf.getSqlName());

        // 过滤器
        this.testRecord = __eval_match(conf.getTestRecord(), true);
        this.testContext = __eval_match(conf.getTestContext(), true);
        this.ignoreRecord = __eval_match(conf.getIgnoreRecord(), false);
        this.ignoreContext = __eval_match(conf.getIgnoreContext(), false);

        // 数据创建
        this.data = WnExplains.parse(conf.getData());

        // 数据剪裁
        if (conf.hasTrimData()) {
            HisConfigTrimData[] tds = conf.getTrimData();
            this.trimData = new HisRuntimeTrimData[tds.length];
            for (int i = 0; i < tds.length; i++) {
                HisConfigTrimData td = tds[i];
                HisRuntimeTrimData rtd = new HisRuntimeTrimData();
                rtd.name = td.getName();
                rtd.maxSize = td.getMaxSize();
                this.trimData[i] = rtd;
            }
        }

        // 数据处理
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

        // 输出
        this.toPipeKey = conf.getTo();
    }

    private WnMatch __eval_match(Object input, boolean dft) {
        if (null != input) {
            return AutoMatch.parse(input, dft);
        }
        return null;
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

    public boolean isMatchRecord(NutBean record, NutBean myContext) {
        if (null != testRecord && !testRecord.match(record)) {
            return false;
        }
        if (null != testContext && !testContext.match(myContext)) {
            return false;
        }
        if (null != ignoreRecord && ignoreRecord.match(record)) {
            return false;
        }
        if (null != ignoreContext && ignoreContext.match(myContext)) {
            return false;
        }
        return true;
    }

    public NutMap createLogRecord(Date now, NutBean myContext, NutBean record) {
        myContext.put("item", record);
        Object re = this.data.explain(myContext);
        NutMap bean = NutMap.WrapAny(re);
        // 有些时候这个 bean 里面的 op_data 可能会太大，当然也可能是其他的字段
        // 为了保证记录能被顺利插入，我们需要对每个字段的值做一些裁剪
        if (null != this.trimData) {
            for (HisRuntimeTrimData td : this.trimData) {
                td.doTrim(bean);
            }
        }

        // 设置额外数据字段
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

    public boolean hasToPipeKey() {
        return !Ws.isBlank(toPipeKey);
    }

    public String getToPipeKey() {
        return toPipeKey;
    }

}
