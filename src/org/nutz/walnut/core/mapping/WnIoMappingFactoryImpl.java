package org.nutz.walnut.core.mapping;

import java.util.Map;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.core.WnIoBM;
import org.nutz.walnut.core.WnIoIndexer;
import org.nutz.walnut.core.WnIoMapping;
import org.nutz.walnut.core.WnIoMappingFactory;

public class WnIoMappingFactoryImpl implements WnIoMappingFactory {

    /**
     * 全局索引管理器，如果映射没声明映射管理器，就用这个
     */
    private WnIoIndexer globalIndexer;;

    /**
     * 全局桶管理器，如果映射没声明桶管理器，就用这个
     */
    private WnIoBM globalBM;

    /**
     * 索引管理器工厂映射
     */
    private Map<String, WnIndexerFactory> indexers;

    /**
     * 桶管理器工厂映射
     */
    private Map<String, WnBMFactory> bms;

    @Override
    public WnIoMapping check(String homeId, String mount) {
        // 首先分析映射
        MountInfo mi = new MountInfo(mount);

        // 准备获取索引管理器和桶管理器
        WnIoIndexer ix;
        WnIoBM bm;

        // 采用全局索引管理器
        if (!mi.hasIndexer()) {
            ix = this.globalIndexer;
        }
        // 获取索引管理器
        else {
            WnIndexerFactory ixFa = indexers.get(mi.ix.type);
            ix = ixFa.load(homeId, mi.ix.arg);
        }

        // 采用全局桶管理器
        if (!mi.hasBM()) {
            bm = this.globalBM;
        }
        // 获取桶管理器
        else {
            WnBMFactory bmFa = bms.get(mi.bm.type);
            bm = bmFa.load(homeId, mi.bm.arg);
        }

        // 组合返回
        return new WnIoMapping(ix, bm);
    }

    @Override
    public WnIoMapping check(WnObj obj) {
        // 木有映射
        if (!obj.isMount()) {
            return new WnIoMapping(globalIndexer, globalBM);
        }
        // 获取其顶级映射
        String homeId = obj.mountRootId();
        String mount = obj.mount();

        // 返回
        return check(homeId, mount);
    }

}
