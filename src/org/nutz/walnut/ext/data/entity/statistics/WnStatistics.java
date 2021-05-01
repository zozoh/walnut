package org.nutz.walnut.ext.data.entity.statistics;

import org.nutz.dao.Cnd;
import org.nutz.dao.Condition;
import org.nutz.dao.Dao;
import org.nutz.dao.util.cri.SimpleCriteria;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.core.indexer.dao.WnDaoQuery;
import org.nutz.walnut.ext.data.entity.statistics.bean.AmsRange;
import org.nutz.walnut.ext.sys.sql.WnDaoAuth;
import org.nutz.walnut.ext.sys.sql.WnDaos;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public abstract class WnStatistics {

    protected WnIo io;

    protected Dao srcDao;

    protected boolean test;

    protected boolean force;

    public WnStatistics(WnStatConfig config, WnIo io, NutMap vars) {
        this.io = io;

        WnDaoAuth srcDaoAuth = WnDaos.loadAuth(io, config.getSrcDao(), vars);
        this.srcDao = WnDaos.get(srcDaoAuth);

    }

    public Condition getCondition(String timeKey, AmsRange range) {
        return getCondition(timeKey, range, null);
    }

    public Condition getCondition(String timeKey, AmsRange range, NutMap query) {
        // 纯粹的时间范围
        if (null == query || query.isEmpty()) {
            SimpleCriteria cri = Cnd.cri();
            cri.where().andGTE(timeKey, range.getBeginInMs()).andLT(timeKey, range.getEndInMs());
            return cri;
        }
        // 带条件的时间范围
        WnQuery q = new WnQuery();
        q.setAll(query);
        q.setv(timeKey, String.format("[%d, %d)", range.getBeginInMs(), range.getEndInMs()));
        WnDaoQuery daoQ = new WnDaoQuery(q);
        return daoQ.getCondition();
    }

    public void updateByParams(ZParams params) {
        this.setForce(params.is("force"));
        this.setTest(params.is("test"));
    }

    public AmsRange getRangeFrom(ZParams params) {
        String date = params.getString("date", "now");
        long endInMs = Wn.evalDateMs(date);

        // 获取时间跨度
        String span = params.getString("span", "7d");
        long duInMs = Wn.msValueOf(span);
        long beginInMs = endInMs - duInMs;

        // 返回
        AmsRange range = new AmsRange();
        range.setBeginInMs(beginInMs);
        range.setEndInMs(endInMs);
        return range;
    }

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    public boolean isTest() {
        return test;
    }

    public void setTest(boolean test) {
        this.test = test;
    }
}
