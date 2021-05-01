package org.nutz.walnut.ext.data.thing.hdl;

import org.nutz.lang.Each;
import org.nutz.lang.Stopwatch;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.data.thing.util.Things;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

@JvmHdlParamArgs(regex = "^(quiet|data)$")
public class thing_clean implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 判断是否静默输出
        boolean isQ = hc.params.is("quiet");

        // 开始计时
        Stopwatch sw = Stopwatch.begin();

        // 找到索引目录
        WnObj oIndex = Things.dirTsIndex(sys, hc);

        // 得到数据目录
        WnObj oData = Things.dirTsData(sys, hc);

        // 跳转
        int skip = hc.params.getInt("skip", 0);

        // 最多删除多少个（默认1000）
        int limit = hc.params.getInt("limit", 1000);

        // 清除残存的数据目录
        if (hc.params.has("data")) {
            this.__clean_data(sys, hc, isQ, limit, skip, oData, oIndex);
        }
        // 清除索引
        else {
            this.__clean_index(sys, hc, isQ, limit, skip, oData, oIndex);
        }

        // 删除完成
        sw.stop();

        // 打印结束
        if (!isQ)
            sys.out.println("All Done in " + sw.toString());
    }

    private void __clean_data(WnSystem sys,
                              JvmHdlContext hc,
                              boolean isQ,
                              int limit,
                              int skip,
                              WnObj oData,
                              WnObj oIndex) {
        // 准备过滤条件
        WnQuery q = Wn.Q.pid(oData);
        q.setv("race", WnRace.DIR);
        q.limit(limit);
        q.skip(skip);
        q.asc("lm");

        // 找到所有迷失的 data
        sys.io.each(q, new Each<WnObj>() {
            public void invoke(int index, WnObj oTD, int length) {
                if (!isQ)
                    sys.out.printf("%s > ", oTD.name());

                // 正常
                if (sys.io.exists(oIndex, oTD.name())) {
                    if (!isQ)
                        sys.out.println("OK");
                }
                // 不正常，要删除
                else {
                    if (!isQ)
                        sys.out.println("!Lost! will be removed!");
                    sys.io.delete(oTD, true);
                }
            }
        });
    }

    private void __clean_index(WnSystem sys,
                               JvmHdlContext hc,
                               boolean isQ,
                               int limit,
                               int skip,
                               WnObj oData,
                               WnObj oIndex) {

        // 准备过滤条件
        WnQuery q = Wn.Q.pid(oIndex);
        q.setv("th_live", -1);
        q.limit(limit);
        q.skip(skip);
        q.asc("lm");

        // 找到所有被标记删除的 Thing 从最后修改时间开始正序删除
        sys.io.each(q, new Each<WnObj>() {
            public void invoke(int index, WnObj oT, int length) {
                if (!isQ)
                    sys.out.printf("rm th:%s > ", oT.id());

                // 删除数据对象
                WnObj oThData = sys.io.fetch(oData, oT.id());
                if (null != oThData) {
                    sys.io.delete(oThData, true);
                    if (!isQ)
                        sys.out.print("Data ");
                }

                // 删除索引
                sys.io.delete(oT);
                if (!isQ)
                    sys.out.println("Index OK");

            }
        });
    }

}
