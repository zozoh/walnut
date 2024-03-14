package com.site0.walnut.ext.data.entity.favor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.nutz.lang.Strings;
import org.nutz.lang.Times;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.entity.JvmRedisEntityExecutor;
import com.site0.walnut.ext.data.entity.RedisEntityPrinter;
import com.site0.walnut.ext.sys.redis.WedisConfig;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.WnPager;
import com.site0.walnut.util.ZParams;

public class cmd_favor extends JvmRedisEntityExecutor<FavorIt> {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {

        ZParams params = ZParams.parse(args, "cqn", "^(ajax|json|rever|ms|quiet|pager)$");

        // 分析参数
        String action = params.val_check(0);
        String uid = params.val_check(1);
        int skip = params.getInt("skip", 0);
        boolean asMs = params.is("ms");
        String df = params.getString("df", "yyyy-MM-dd HH:mm:ss");
        String loadObjBy = params.getString("obj");
        boolean loadObj = !Strings.isBlank(loadObjBy);

        // 计算默认值
        Object dv = params.get("dv", null);
        // 指定为 null
        if ("nil".equals(dv)) {
            dv = null;
        }
        // 看来是数字
        else if (null != dv && dv.toString().matches("^([0-9]+)$")) {
            dv = Long.parseLong(dv.toString());
        }
        final Object dftVal = dv;

        // 准备配置
        WedisConfig conf = this.prepareConfig(sys, params, "favor");

        // 准备接口
        FavorApi api = new WnRedisFavorService(conf);

        // 准备返回
        Object re;

        // favor yes
        if ("yes".equals(action)) {
            String[] taIds = params.subvals(2);
            re = api.favorIt(uid, taIds);
        }
        // favor no
        else if ("no".equals(action)) {
            String[] taIds = params.subvals(2);
            re = api.unfavor(uid, taIds);
        }
        // favor all
        else if ("all".equals(action)) {
            // 默认最多取100个
            int limit = params.getInt("limit", 100);

            // 得到列表
            List<FavorIt> list;
            if (params.is("rever")) {
                list = api.revAll(uid, skip, limit);
            } else {
                list = api.getAll(uid, skip, limit);
            }

            // 格式化输出列表
            if (params.is("ajax") || params.is("json")) {
                List<? extends Object> list2;
                List<NutMap> list3 = new ArrayList<NutMap>(list.size());
                for (FavorIt it : list) {
                    NutMap it2 = new NutMap();
                    String taId = it.getTarget();
                    Object time = formatValue(asMs, df, dftVal, it.getTime());
                    it2.put("target", taId);
                    it2.put("time", time);
                    // n+1 读取对象元数据内容
                    if (loadObj) {
                        if ("id".equals(loadObjBy)) {
                            WnObj o = sys.io.get(taId);
                            it2.put("obj", o);
                        }
                    }
                    list3.add(it2);
                }
                list2 = list3;

                // 计算一下总数
                long count = api.count(uid);

                // 计算翻页器
                WnPager wp = new WnPager(limit, skip);
                wp.setSumCount(count);

                // 设置返回值：带翻页
                if (params.is("pager")) {
                    re = Cmds.createQueryResult(wp, list2);
                }
                // 设置返回值：直接列表
                else {
                    re = list2;
                }
            }
            // 维持原样，等打印的时候再格式化吧
            else {
                re = list;
            }

        }
        // favor sum
        else if ("count".equals(action)) {
            re = api.count(uid);
        }
        // favor is
        else if ("when".equals(action)) {
            String[] taIds = params.subvals(2);
            long[] amss = api.whenFavor(uid, taIds);

            NutMap map = new NutMap();

            // 转换
            for (int i = 0; i < amss.length; i++) {
                long ams = amss[i];
                Object val = formatValue(asMs, df, dftVal, ams);

                // 计入
                map.put(taIds[i], val);
            }

            // 设置返回值
            re = map;
        }
        // 不支持的动作
        else {
            throw Er.create("e.cmd.favor.invalidAction", action);
        }

        // 输出
        if (!params.is("quiet")) {
            String fmt = params.get("out", "%d) %s + %s");
            output(sys, params, re, new RedisEntityPrinter<FavorIt>() {
                public void print(FavorIt fi, int i) {
                    long ams = fi.getTime();
                    Object val = formatValue(asMs, df, dftVal, ams);
                    sys.out.printlnf(fmt, i, val, fi.getTarget());
                }
            });
        }
    }

    private Object formatValue(boolean asMs, String df, Object dftVal, long ams) {
        Object val = null;
        // 采用默认
        if (ams <= 0) {
            val = dftVal;
        }
        // 格式化值: 毫秒数
        else if (asMs) {
            val = ams;
        }
        // 阅读文字: 日子
        else {
            Date d = Times.D(ams);
            val = Times.format(df, d);
        }
        return val;
    }

}
