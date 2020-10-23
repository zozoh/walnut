package org.nutz.walnut.ext.entity.statistics.agg;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.nutz.dao.Condition;
import org.nutz.dao.Dao;
import org.nutz.dao.entity.Entity;
import org.nutz.dao.entity.Record;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Each;
import org.nutz.lang.Lang;
import org.nutz.lang.Times;
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.entity.statistics.WnStatistics;
import org.nutz.walnut.ext.entity.statistics.bean.NamedAmsRange;
import org.nutz.walnut.ext.entity.statistics.bean.TimeUnit;
import org.nutz.walnut.ext.entity.statistics.bean.AmsRange;
import org.nutz.walnut.ext.sql.WnDaoAuth;
import org.nutz.walnut.ext.sql.WnDaos;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public class WnStatAgg extends WnStatistics {

    private static Log log = Logs.get();

    private WnStatAggConfig config;

    private Dao targetDao;

    private WnObj oMarks;

    public WnStatAgg(WnStatAggConfig config, WnSystem sys) {
        this(config, sys.io, sys.session.getVars());
    }

    public WnStatAgg(WnStatAggConfig config, WnIo io, NutMap vars) {
        super(config, io, vars);

        this.config = config;
        WnDaoAuth targetDaoAuth = WnDaos.loadAuth(io, config.getTargetDao(), vars);
        this.targetDao = WnDaos.get(targetDaoAuth);

        if (this.config.hasMarkBy()) {
            String aph = Wn.normalizeFullPath(config.getMarkBy(), vars);
            this.oMarks = io.createIfNoExists(null, aph, WnRace.FILE);
        }

    }

    public void invoke(AmsRange range, EachAggregation callback) {
        invoke(range.getBeginInMs(), range.getEndInMs(), callback);
    }

    /**
     * @param beginInMs
     *            时间段开始的绝对毫秒数（会被自动对齐）
     * @param endInMs
     *            时间段结束的绝对毫秒数（会被自动对齐）
     * @param callback
     *            回调。如果为 null 则不会生成聚集列表（可以省点内存）
     */
    public void invoke(long beginInMs, long endInMs, EachAggregation callback) {
        // 读取标志
        NutMap marks = null;
        if (null != this.oMarks) {
            marks = io.readJson(oMarks, NutMap.class);
        }
        // 确保这个类不是空的，因为在循环里总是要判断
        if (null == marks) {
            marks = new NutMap();
        }

        // 保存之前的 mark， 只有更新的时候才保存
        NutMap oldMarks = marks.duplicate();

        String taTimeKey = config.getTargetTimeBy("tams");

        // 获取时间范围
        String tuName = config.getTimeUnit("day");
        TimeUnit tu = TimeUnit.checkInstance(tuName);

        // 对齐总时间窗口，如果要强制计算，则先清空一波数据
        AmsRange range = tu.padRange(beginInMs, endInMs);
        if (force && range.isMakeSense()) {
            Condition cnd = this.getCondition(taTimeKey, range);
            int count = targetDao.clear(config.getTargetTableName(), cnd);
            if (log.isDebugEnabled()) {
                log.debugf("statistic aggregate force clean %d records", count);
            }
        }

        // 拆分时间窗口
        List<NamedAmsRange> ranges = tu.splitRanges(range);

        // 循环检查记录
        for (NamedAmsRange nar : ranges) {
            // 这个是本时间段的返回列表
            List<NutBean> list;

            // 已经有了标志且不是强制计算，就直接查询一下
            if (!force && marks.getBoolean(nar.getName())) {
                if (null != callback) {
                    Condition cnd = this.getCondition(taTimeKey, nar);
                    list = new LinkedList<>();
                    targetDao.each(config.getTargetTableName(), cnd, new Each<Record>() {
                        public void invoke(int index, Record rec, int length) {
                            NutBean li = NutMap.WRAP(rec);
                            list.add(li);
                        }
                    });
                    callback.invoke(nar.getName(), list);
                }
                continue;
            }

            // 执行聚集
            list = this.aggregate(nar);
            if (null != callback) {
                callback.invoke(nar.getName(), list);
            }

            // 执行一下保存
            if (!test) {
                this.saveResult(list);
            }

            // 标志
            if (!list.isEmpty()) {
                marks.put(nar.getName(), true);
            }
        }

        // 写入目标，并且更新mark
        if (!test && null != this.oMarks) {
            if (!marks.equals(oldMarks)) {
                // 看看标记文件标记的时间窗口
                long now = System.currentTimeMillis();
                String markRemain = config.getMarkRemain("1100d");
                long duInMs = Wn.msValueOf(markRemain);
                AmsRange markRange = tu.padRange(now - duInMs, now);

                // 收集一个新的 Marks
                NutMap newMarks = new NutMap();
                for (Map.Entry<String, Object> en : marks.entrySet()) {
                    String key = en.getKey();
                    if (markRange.isInRange(key)) {
                        newMarks.put(key, en.getValue());
                    }
                }

                // 写入
                io.writeJson(oMarks, newMarks, JsonFormat.nice());
            }
        }

    }

    private void saveResult(List<NutBean> beans) {
        // 防守一道
        if (null == beans || beans.isEmpty())
            return;
        // 分析实体
        NutBean firstBean = Lang.first(beans);

        Entity<NutBean> en = targetDao.getEntityHolder()
                                      .makeEntity(config.getTargetTableName(), firstBean);

        // 快速插入
        targetDao.fastInsert(en, beans);
    }

    private List<NutBean> aggregate(AmsRange ar) {
        // 首先准备聚集的 Map
        Map<String, NutBean> map = new HashMap<>();

        // 准备键值模板
        Tmpl keyBy = Tmpl.parse(config.getGroupBy());

        // 准备一下查询条件
        String timeKey = config.getSrcTimeBy("ct");
        NutMap q = config.getQuery();
        Condition cnd = this.getCondition(timeKey, ar, q);

        // 准备一下 sumbBy
        AggSumBy sumBy = config.getAggSumBy();

        // 循环全部记录，并进行归纳
        String fmt = config.getDateFormat("yyyy-MM-dd");
        String tableName = config.getSrcTableName();
        srcDao.each(tableName, cnd, null, new Each<Record>() {
            public void invoke(int index, Record rec, int length) {
                NutMap ctx = NutMap.WRAP(rec);

                // 看看累加值，默认为 1
                int val = sumBy.getSum(ctx);
                if (val == 0) {
                    return;
                }

                // 计算时间以便分组
                long ms = ctx.getLong(timeKey); // 因为有 SQL 条件，这个值一定是有的
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(ms);
                ctx.put("@ams", ms);
                ctx.put("@year", c.get(Calendar.YEAR));
                ctx.put("@month", c.get(Calendar.MONTH));
                ctx.put("@date", c.get(Calendar.DATE));
                ctx.put("@week", c.get(Calendar.WEEK_OF_YEAR));
                ctx.put("@day", c.get(Calendar.DAY_OF_WEEK));
                ctx.put("@hour", c.get(Calendar.HOUR_OF_DAY));
                ctx.put("@minute", c.get(Calendar.MINUTE));
                ctx.put("@second", c.get(Calendar.SECOND));
                ctx.put("@format", Times.format(fmt, c.getTime()));

                // 计算分组的 Key
                String key = keyBy.render(ctx);
                NutBean bean = map.get(key);

                // 如果是第一个记录，那么对齐时间至区间开始时间
                if (null == bean) {
                    bean = new NutMap();
                    bean.putAll(ctx);
                    // 第一个记录，设置区间时间
                    bean.put("@groupKey", key);
                    bean.put("@begin", ar.getBeginInMs());
                    bean.put("@end", ar.getEndInMs());

                    // 记入
                    map.put(key, bean);
                }

                // 获取累加值
                bean.intIncrement("@value", val);

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

}
