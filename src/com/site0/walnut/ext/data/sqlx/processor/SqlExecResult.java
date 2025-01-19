package com.site0.walnut.ext.data.sqlx.processor;

import java.util.List;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

public class SqlExecResult {

    public int updateCount;

    public int[] batchResult;

    public int batchTotal;

    /**
     * 对于执行操作，如果有 fetchBack，查询回来的结果存放再这里
     */
    public List<NutBean> list;

    public NutBean toBriefBean() {
        NutMap bean = new NutMap();
        bean.put("updateCount", updateCount);
        bean.put("batchTotal", batchTotal);
        return bean;
    }

    public void mergeWith(SqlExecResult re) {
        this.updateCount += re.updateCount;
        this.batchTotal += re.batchTotal;
    }

}
