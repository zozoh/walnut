package com.site0.walnut.ext.data.entity.history.fake;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.nutz.json.Json;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.ext.data.entity.history.HistoryRecord;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;

public class HistoryFaker {

    private static Random RAD = new Random(System.currentTimeMillis());

    private WnIo io;

    private HistoryFakeConfig config;

    private NutMap vars;

    private Map<String, NutBean[]> schema;

    private long beginInMs;

    private long endInMs;

    public HistoryFaker() {}

    public HistoryFaker(HistoryFakeConfig config, WnSystem sys) {
        this.config = config;
        this.io = sys.io;
        this.vars = sys.session.getVars();
    }

    public HistoryFaker(HistoryFakeConfig config, WnIo io, String homePath) {
        this.config = config;
        this.io = io;
        this.vars = Wlang.map("HOME", homePath);
    }

    public List<HistoryRecord> getRecords(int seq, int n, int pad) {
        List<HistoryRecord> list = new ArrayList<>(n);

        for (int i = 0; i < n; i++) {
            HistoryRecord his = this.createHistoryRecord(seq + i, pad);
            list.add(his);
        }

        return list;
    }

    public HistoryRecord createHistoryRecord(int seq, int pad) {
        HistoryRecord his = new HistoryRecord();

        // 缓存得到 schemaBean
        Map<String, NutBean> beans = new HashMap<>();

        // 设置记录的时间
        // 得到一个时间范围内的随机毫秒数
        long duInMs = endInMs - beginInMs;
        double ra = RAD.nextDouble();
        long offInMs = (long) ((double) duInMs * ra);
        his.setCreateTime(beginInMs + offInMs);

        // 生成伪序号
        String padSeq = Strings.alignRight(seq, pad, '0');

        // 依次设置值
        his.setId(_VAL(config.getId(), padSeq, beans));
        his.setUserId(_VAL(config.getUserId(), padSeq, beans));
        his.setUserName(_VAL(config.getUserName(), padSeq, beans));
        his.setUserType(_VAL(config.getUserType(), padSeq, beans));
        his.setTargetId(_VAL(config.getTargetId(), padSeq, beans));
        his.setTargetName(_VAL(config.getTargetName(), padSeq, beans));
        his.setTargetType(_VAL(config.getTargetType(), padSeq, beans));
        his.setOperation(_VAL(config.getOperation(), padSeq, beans));
        his.setMore(_VAL(config.getMore(), padSeq, beans));

        return his;
    }

    private String _VAL(HistoryFakeField fld, String padSeq, Map<String, NutBean> beans) {
        // 防守
        if (null == fld) {
            return null;
        }
        // 生成模板
        if (fld.hasGenTmpl()) {
            return fld.getGenTmpl().render(Wlang.map("seq", padSeq));
        }
        // 固定值
        if (fld.hasValue()) {
            return fld.getValue();
        }
        // 随机挑选值
        if (fld.hasCans()) {
            return fld.getOneCan();
        }
        // 从 Bean 挑选一个值
        if (fld.hasKey() && fld.hasSchema()) {
            String schemaName = fld.getSchema();
            NutBean bean = beans.get(schemaName);
            // 如果木有的话，那么尝试从 schema 里获取一个
            if (null == bean) {
                NutBean[] list = schema.get(schemaName);
                if (null != list && list.length > 0) {
                    int index = RAD.nextInt(list.length);
                    bean = list[index];
                    beans.put(schemaName, bean);
                }
            }
            // 嗯，得到键
            if (null != bean) {
                return bean.getString(fld.getKey());
            }
        }
        // 那就是木有值咯
        return null;
    }

    public void prepareSchema() {
        // 首先重置一下 schema 的缓存
        this.schema = new HashMap<>();

        // 守一道
        if (!config.hasSchema()) {
            return;
        }

        // 依次从配置里加载 schema
        for (Map.Entry<String, HistoryFakeSchema> en : config.getSchema().entrySet()) {
            String key = en.getKey();
            HistoryFakeSchema hfs = en.getValue();

            // 找到路径
            String aph = Wn.normalizeFullPath(hfs.getPath(), vars);
            WnObj o = io.fetch(null, aph);
            if (null == o) {
                throw Er.create("e.his.fake.schema.InvalidPath", hfs.getPath());
            }

            // 如果是文件，当作 JSON
            if (o.isFILE()) {
                String json = io.readText(o);
                NutBean[] list = Json.fromJson(NutMap[].class, json);
                this.schema.put(key, list);
            }
            // 否则就是目录，准备查询
            else {
                WnQuery q = Wn.Q.pid(o);
                if (hfs.hasQuery()) {
                    q.setAll(hfs.getQuery());
                }
                if (hfs.hasSort()) {
                    q.sort(hfs.getSort());
                }
                q.limit(hfs.getLimit());
                q.skip(hfs.getSkip());
                List<WnObj> list = io.query(q);
                WnObj[] arry = list.toArray(new WnObj[list.size()]);
                this.schema.put(key, arry);
            }
        }
    }

    public WnIo getIo() {
        return io;
    }

    public void setIo(WnIo io) {
        this.io = io;
    }

    public HistoryFakeConfig getConfig() {
        return config;
    }

    public void setConfig(HistoryFakeConfig config) {
        this.config = config;
    }

    public NutMap getVars() {
        return vars;
    }

    public void setVars(NutMap vars) {
        this.vars = vars;
    }

    public Map<String, NutBean[]> getSchema() {
        return schema;
    }

    public void setSchema(Map<String, NutBean[]> schema) {
        this.schema = schema;
    }

    public long getBeginInMs() {
        return beginInMs;
    }

    public void setBeginInMs(long beginInMs) {
        this.beginInMs = beginInMs;
    }

    public long getEndInMs() {
        return endInMs;
    }

    public void setEndInMs(long endInMs) {
        this.endInMs = endInMs;
    }

}
