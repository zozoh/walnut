package org.nutz.walnut.ext.truck.hdl;

import org.nutz.walnut.api.io.WnIoIndexer;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.core.WnIoBM;
import org.nutz.walnut.core.WnIoMapping;
import org.nutz.walnut.core.WnIoMappingFactory;
import org.nutz.walnut.ext.truck.TruckContext;
import org.nutz.walnut.ext.truck.TruckFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public class truck_from extends TruckFilter {

    @Override
    protected void process(WnSystem sys, TruckContext tc, ZParams params) {
        tc.limit = params.getInt("limit", 1000);
        tc.skip = params.getInt("skip", 0);
        tc.sort = params.getMap("sort");
        tc.match = params.getMap("match");

        String dirPath = params.val_check(0);
        WnObj oDir = Wn.checkObj(sys, dirPath);

        // 准备映射工厂
        WnIoMappingFactory mappings = sys.io.getMappingFactory();

        // 默认的映射
        WnIoMapping mapping = mappings.checkMapping(oDir);

        // 如果当前用了全局索引管理器，那么相当于使用了 "mongo"
        if (mapping.getIndexer() == mappings.getGlobalIndexer()) {
            params.setv("index", "mongo");
        }

        // 指定了索引管理器
        if (params.hasString("index")) {
            String str = params.getString("index");
            WnIoIndexer ix = mappings.loadIndexer(oDir, str);
            mapping.setIndexer(ix);
        }

        // 指定了桶管理器
        if (params.hasString("bm")) {
            String str = params.getString("bm");
            WnIoBM bm = mappings.loadBM(oDir, str);
            mapping.setBucketManager(bm);
        }

        // 设置到上下文
        tc.fromDir = oDir;
        tc.fromIndexer = mapping.getIndexer();
        tc.fromBM = mapping.getBucketManager();
    }

}
