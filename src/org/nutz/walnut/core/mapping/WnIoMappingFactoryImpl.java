package org.nutz.walnut.core.mapping;

import java.util.Map;

import org.nutz.lang.Strings;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.core.WnIoBM;
import org.nutz.walnut.core.WnIoIndexer;
import org.nutz.walnut.core.WnIoMapping;
import org.nutz.walnut.core.WnIoMappingFactory;
import org.nutz.walnut.core.bean.WnObjMapping;

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

    private WnIoMapping __check_mapping(WnObj oHome, String mount) {
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
            ix = ixFa.load(oHome, mi.ix.arg);
        }

        // 采用全局桶管理器
        if (!mi.hasBM()) {
            bm = this.globalBM;
        }
        // 获取桶管理器
        else {
            WnBMFactory bmFa = bms.get(mi.bm.type);
            bm = bmFa.load(oHome, mi.bm.arg);
        }

        // 组合返回
        return new WnIoMapping(ix, bm);
    }

    @Override
    public WnIoMapping checkMapping(String homeId, String mount) {
        // 获取顶端映射对象
        WnObj oHome = null;
        if (!Strings.isBlank(homeId)) {
            oHome = globalIndexer.checkById(homeId);
        }

        // 获取映射
        return __check_mapping(oHome, mount);
    }

    @Override
    public WnIoMapping checkMapping(WnObj obj) {
        // 木有映射
        if (!obj.isMount()) {
            return getGlobalMapping();
        }
        // 获取其顶级映射
        String homeId = obj.mountRootId();
        String mount = obj.mount();

        // 自己就是顶端映射对象
        if (obj.isSameId(homeId)) {
            return __check_mapping(obj, mount);
        }

        // 返回
        return checkMapping(homeId, mount);
    }

    @Override
    public WnObj getRoot() {
        return this.globalIndexer.getRoot();
    }

    @Override
    public WnIoMapping getGlobalMapping() {
        return new WnIoMapping(globalIndexer, globalBM);
    }

    @Override
    public WnIoIndexer getGlobalIndexer() {
        return globalIndexer;
    }

    @Override
    public WnObjMapping checkById(String id) {
        WnObjMapping om = new WnObjMapping(id);
        // 无论怎样，先设置一个全局映射
        om.setGlobalMapping(this.getGlobalMapping());

        // 两段式 ID，尝试获取子映射
        if (om.hasHomeId()) {
            WnObj oHome = globalIndexer.checkById(om.getHomeId());
            if (!oHome.isMount()) {
                throw Er.create("e.io.weirdid.HomeNotMount", id);
            }
            om.setHome(oHome);
            WnIoMapping mapping = checkMapping(oHome);
            om.setMapping(mapping);
        }
        // 非两段式ID
        else {
            om.setHome(globalIndexer.getRoot());
        }
        return om;
    }

}
