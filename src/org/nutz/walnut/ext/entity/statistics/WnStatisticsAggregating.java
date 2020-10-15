package org.nutz.walnut.ext.entity.statistics;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.entity.Record;
import org.nutz.dao.util.cri.SimpleCriteria;
import org.nutz.lang.Each;
import org.nutz.lang.Strings;
import org.nutz.lang.Times;
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.entity.statistics.bean.MarkRange;
import org.nutz.walnut.ext.entity.statistics.bean.MarkUnit;
import org.nutz.walnut.ext.sql.WnDaoAuth;
import org.nutz.walnut.ext.sql.WnDaos;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public class WnStatisticsAggregating {

    private WnStatAggreConfig config;

    private Dao srcDao;

    private Dao targetDao;

    private WnIo io;

    private WnObj oMarks;

    private boolean test;

    private boolean force;

    public WnStatisticsAggregating(WnStatAggreConfig config, WnSystem sys) {
        this(config, sys.io, sys.session.getVars());
    }

    public WnStatisticsAggregating(WnStatAggreConfig config, WnIo io, NutMap vars) {
        this.config = config;
        this.io = io;

        WnDaoAuth srcDaoAuth = WnDaos.loadAuth(io, config.getSrcDao(), vars);
        this.srcDao = WnDaos.get(srcDaoAuth);

        WnDaoAuth targetDaoAuth = WnDaos.loadAuth(io, config.getTargetDao(), vars);
        this.targetDao = WnDaos.get(targetDaoAuth);

        if (this.config.hasMarkBy()) {
            String aph = Wn.normalizeFullPath(config.getMarkBy(), vars);
            this.oMarks = io.check(null, aph);
        }

    }

    public void invoke(long beginInMs, long endInMs, EachAggregation callback) {
        // 防守一道
        if (null == callback) {
            return;
        }
        // 读取标志
        NutMap marks;
        if (null != this.oMarks) {
            marks = io.readJson(oMarks, NutMap.class);
        } else {
            marks = new NutMap();
        }

        // 获取时间范围
        String by = Strings.sBlank(config.getMarkBy(), "day");
        MarkUnit mu = MarkUnit.checkInstance(by);
        List<MarkRange> mrs = mu.evalRanges(beginInMs, endInMs);

        // 循环检查记录
        for (MarkRange mr : mrs) {
            // 已经有了标志且不是强制计算，就无视
            if (!force && marks.getBoolean(mr.getName())) {
                continue;
            }

            // 执行聚集
            List<NutBean> list = this.aggregate(mr);
            callback.invoke(mr.getName(), list);

            // 标志
            marks.put(mr.getName(), true);
        }

        // TODO 这里准备写入目标，并且更新mark
        if (!test) {

        }

    }

    private List<NutBean> aggregate(MarkRange mr) {
        // 首先准备聚集的 Map
        Map<String, NutBean> map = new HashMap<>();

        // 准备键值模板
        Tmpl keyBy = Tmpl.parse(config.getGroupBy());

        // 准备一下查询条件
        String timeKey = Strings.sBlank(config.getDateTimeBy(), "ct");
        SimpleCriteria cri = Cnd.cri();
        cri.where().andGTE(timeKey, mr.getBeginInMs()).andLT(timeKey, mr.getEndInMs());

        // 循环全部记录，并进行归纳
        String tableName = config.getSrcTableName();
        srcDao.each(tableName, cri, null, new Each<Record>() {
            public void invoke(int index, Record rec, int length) {
                NutMap ctx = NutMap.WRAP(rec);
                String key = keyBy.render(ctx);
                NutBean bean = map.get(key);
                if (null == bean) {
                    bean = new NutMap();
                    bean.putAll(ctx);
                    // 第一个记录，将计算时间
                    String fmt = Strings.sBlank(config.getDateFormat(), "yyyy-MM-dd");
                    long ms = ctx.getLong(timeKey);
                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(ms);
                    bean.put("@key", key);
                    bean.put("@ams", ms);
                    bean.put("@year", c.get(Calendar.YEAR));
                    bean.put("@month", c.get(Calendar.MONTH));
                    bean.put("@date", c.get(Calendar.DATE));
                    bean.put("@week", c.get(Calendar.WEEK_OF_YEAR));
                    bean.put("@day", c.get(Calendar.DAY_OF_WEEK));
                    bean.put("@hour", c.get(Calendar.HOUR_OF_DAY));
                    bean.put("@minute", c.get(Calendar.MINUTE));
                    bean.put("@second", c.get(Calendar.SECOND));
                    bean.put("@format", Times.format(fmt, c.getTime()));
                    // 记入
                    map.put(key, bean);
                }

                // 获取累加值
                bean.intIncrement("@value");

            }
        }, "*");

        // 归纳完毕，准备生成结果
        List<NutBean> list = new ArrayList<>(map.size());
        for (NutBean bean : map.values()) {
            NutMap li = (NutMap) Wn.explainObj(bean, config.getMapping());
            list.add(li);
        }
        return list;
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
