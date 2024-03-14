package com.site0.walnut.ext.data.entity.statistics.sum;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.nutz.dao.Condition;
import org.nutz.dao.entity.Record;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Each;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.Times;
import com.site0.walnut.util.tmpl.WnTmpl;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.ext.data.entity.statistics.WnStatistics;
import com.site0.walnut.ext.data.entity.statistics.bean.AmsRange;
import com.site0.walnut.ext.data.entity.statistics.bean.TimeUnit;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.ZParams;

public class WnStatSum extends WnStatistics {

    private WnStatSumConfig config;

    private WnObj oCacheDir;

    private WnTmpl cacheName;

    private NutMap sortMap;

    private int topNumber;

    private String othersName;

    public WnStatSum(WnStatSumConfig config, WnSystem sys) {
        this(config, sys.io, sys.session.getVars());
    }

    public WnStatSum(WnStatSumConfig config, WnIo io, NutMap vars) {
        super(config, io, vars);

        this.config = config;

        if (this.config.hasCacheDir()) {
            String aph = Wn.normalizeFullPath(config.getCacheDir(), vars);
            this.oCacheDir = io.createIfNoExists(null, aph, WnRace.DIR);
            this.cacheName = WnTmpl.parse(config.getCacheName("web-sum--${@dateBegin}-${@dateEnd}.json"));
        }
    }

    @Override
    public void updateByParams(ZParams params) {
        super.updateByParams(params);

        // 排序的设定
        String sort = params.getString("sort");
        if (!Strings.isBlank(sort)) {
            sortMap = Lang.map(sort);
        }

        // Top 10
        topNumber = params.getInt("top", -1);

        // Others Name
        othersName = params.getString("others");
    }

    public List<NutMap> normalizeList(List<NutMap> list) {
        // 排序
        if (null != sortMap && sortMap.size() > 0) {
            String sortKey = sortMap.keySet().iterator().next();
            // 1 从小到大， -1 从大到小
            int sortVal = sortMap.getInt(sortKey);
            list.sort(new Comparator<NutBean>() {
                @SuppressWarnings({"rawtypes", "unchecked"})
                public int compare(NutBean o1, NutBean o2) {
                    Object v1 = o1.get(sortKey);
                    Object v2 = o2.get(sortKey);
                    if (null == v1 || null == v2)
                        return 0;
                    if (v1 instanceof Comparable<?>) {
                        if (v2 instanceof Comparable<?>) {
                            Comparable c1 = (Comparable) v1;
                            Comparable c2 = (Comparable) v2;
                            return c1.compareTo(c2) * sortVal * -1;
                        }
                    }
                    return 0;
                }
            });

            // 从中截取 Top10
            if (this.topNumber > 0) {
                String nameBy = config.getNameBy("name");
                String valueBy = config.getValueBy("value");
                int othersV = 0;
                int i = 0;
                List<NutMap> list2 = new ArrayList<>(this.topNumber + 1);
                for (NutMap bean : list) {
                    if (i < this.topNumber) {
                        list2.add(bean);
                    } else {
                        int v = bean.getInt(valueBy, 0);
                        othersV += v;
                    }
                    i++;
                }

                // 收集 Others
                if (!Strings.isBlank(this.othersName)) {
                    NutMap others = new NutMap();
                    others.put(nameBy, this.othersName);
                    others.put(valueBy, othersV);
                    list2.add(others);
                }
                // 嗯，返回新的列表
                return list2;
            }

        }
        // 原样输出
        return list;
    }

    public List<NutMap> invoke(AmsRange range) {
        return invoke(range.getBeginInMs(), range.getEndInMs());
    }

    /**
     * @param beginInMs
     *            时间段开始的绝对毫秒数（会被自动对齐）
     * @param endInMs
     *            时间段结束的绝对毫秒数（会被自动对齐）
     */
    public List<NutMap> invoke(long beginInMs, long endInMs) {
        // 获取时间范围
        String by = config.getTimeUnit("day");
        TimeUnit mu = TimeUnit.checkInstance(by);
        AmsRange ar = mu.padRange(beginInMs, endInMs);

        // 获取缓存文件名
        String dfmt = config.getCacheDateFormat("yyyyMMdd");
        NutMap fctx = new NutMap();
        fctx.put("@dateBegin", ar.formatBegin(dfmt));
        fctx.put("@dateEnd", ar.formatEnd(dfmt));
        String fname = this.cacheName.render(fctx);

        // 这个就是缓存文件
        WnObj oCache;

        // 是否存在缓存呢?
        if (!force) {
            oCache = io.fetch(oCacheDir, fname);
            if (null != oCache) {
                String json = io.readText(oCache);
                List<NutMap> list = Json.fromJsonAsList(NutMap.class, json);
                if (null == list) {
                    return new LinkedList<>();
                }
                return list;
            }
        }

        // 首先准备聚集的 Map
        Map<String, NutBean> map = new HashMap<>();

        // 准备键值模板
        WnTmpl keyBy = WnTmpl.parse(config.getGroupBy());
        String sumBy = config.getSumBy("val");

        // 准备一下查询条件
        String timeKey = config.getSrcTimeBy("tams");
        NutMap q = config.getQuery();
        Condition cnd = this.getCondition(timeKey, ar, q);

        // 循环源数据表
        String fmt = config.getDateFormat("yyyy-MM-dd");
        String tableName = config.getSrcTableName();
        srcDao.each(tableName, cnd, new Each<Record>() {
            public void invoke(int index, Record rec, int length) {
                NutMap ctx = NutMap.WRAP(rec);

                // 看看要加总什么值
                int val = ctx.getInt(sumBy, 0);

                // 0 值就无视吧
                if (val == 0)
                    return;

                // 获取分组键
                String key = keyBy.render(ctx);
                NutBean bean = map.get(key);
                if (null == bean) {
                    bean = new NutMap();
                    bean.putAll(ctx);
                    // 第一个记录，将计算时间
                    long ms = ctx.getLong(timeKey, 0);
                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(ms);
                    bean.put("@groupKey", key);
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
                bean.intIncrement("@value", val);
            }
        });

        // 归纳完毕，准备生成结果
        List<NutMap> list = new ArrayList<>(map.size());
        for (NutBean bean : map.values()) {
            NutMap li = (NutMap) Wn.explainObj(bean, config.getMapping());
            list.add(li);
        }

        // 记入缓存：到了这里，那么不管是不是 force,都是要写一遍的
        if (!test && null != this.oCacheDir) {
            oCache = io.createIfNoExists(oCacheDir, fname, WnRace.FILE);
            // 查询结果缓存 3 天
            oCache.expireTime(System.currentTimeMillis() + 86400000L * 3);
            io.set(oCache, "^(expi)$");
            // 记入缓存结果
            String json = Json.toJson(list, JsonFormat.compact().setIgnoreNull(true));
            io.writeText(oCache, json);
        }

        // 搞定
        return list;
    }

}
