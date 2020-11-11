package org.nutz.walnut.ext.truck.impl;

import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.ext.truck.TruckContext;
import org.nutz.walnut.ext.truck.TruckMode;

public class WnTruckService {

    public WnTruckService() {}

    private EachTruck genCallback(TruckContext tc) {
        if (TruckMode.BOTH == tc.mode) {
            return new EachTruckBoth(tc);
        }
        if (TruckMode.INDEXER == tc.mode) {
            return new EachTruckIndexer(tc);
        }
        if (TruckMode.BM == tc.mode) {
            return new EachTruckBM(tc);
        }
        throw Er.create("e.cmd.truck.invalid_mode", tc.mode);
    }

    public void lanuch(TruckContext tc) {
        // 防守
        if (null == tc.io) {
            return;
        }

        // 怎么也得有个源
        if (null == tc.fromDir) {
            return;
        }

        // 输出目标与源同源
        if (null == tc.toDir) {
            tc.toDir = tc.fromDir.clone();
            // // 更新一下输出目录对象的内置索引管理器
            // if (null != tc.toIndexer) {
            // if (tc.toDir instanceof WnIoObj) {
            // ((WnIoObj) tc.toDir).setIndexer(tc.toIndexer);
            // }
            // }
        }

        // 准备查询条件
        WnQuery q = tc.getQuery();

        // 准备回调
        EachTruck callback = this.genCallback(tc);

        // 依次循环数据
        tc.fromIndexer.each(q, callback);
    }

}
