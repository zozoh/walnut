package com.site0.walnut.core.mapping;

import java.util.HashMap;

import org.nutz.lang.Strings;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnIoIndexer;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.core.WnIoBM;
import com.site0.walnut.core.WnIoMapping;
import com.site0.walnut.core.WnIoMappingFactory;
import com.site0.walnut.core.bean.WnObjMapping;

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
    private HashMap<String, WnIndexerFactory> indexers;

    /**
     * 桶管理器工厂映射
     */
    private HashMap<String, WnBMFactory> bms;

    private WnIoMapping __check_mapping(WnObj oMntRoot, String mount) {
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
            ix = loadIndexer(oMntRoot, mi.ix);
        }

        // 采用全局桶管理器
        if (!mi.hasBM()) {
            bm = this.globalBM;
        }
        // 获取桶管理器
        else {
            bm = loadBM(oMntRoot, mi.bm);
        }

        // 组合返回
        return new WnIoMapping(ix, bm);
    }

    @Override
    public WnIoBM loadBM(WnObj oHome, String bmMount) {
        MountInfo.Item mibm = MountInfo.parseItem(bmMount);
        return loadBM(oHome, mibm);
    }

    @Override
    public WnIoBM loadBM(WnObj oHome, MountInfo.Item mibm) {
        WnBMFactory bmFa = bms.get(mibm.type);
        if (null == bmFa) {
            throw Er.create("e.io.mapping.WnBMFactoryNotFound", mibm.toString());
        }
        return bmFa.load(oHome, mibm.arg);
    }

    @Override
    public WnIoIndexer loadIndexer(WnObj oHome, String ixMount) {
        MountInfo.Item miix = MountInfo.parseItem(ixMount);
        return loadIndexer(oHome, miix);
    }

    @Override
    public WnIoIndexer loadIndexer(WnObj oHome, MountInfo.Item miix) {
        WnIndexerFactory ixFa = indexers.get(miix.type);
        if (null == ixFa) {
            throw Er.create("e.io.mapping.WnIndexerFactoryNotFound", miix.toString());
        }
        return ixFa.load(oHome, miix.arg);
    }

    @Override
    public WnIoMapping checkMapping(String mountRootId, String mount) {
        // 获取顶端映射对象
        WnObj oMntRoot = null;
        if (!Strings.isBlank(mountRootId)) {
            oMntRoot = globalIndexer.checkById(mountRootId);
        }

        // 获取映射
        return __check_mapping(oMntRoot, mount);
    }

    @Override
    public WnIoMapping checkMapping(WnObj obj) {
        // 木有映射
        if (!obj.isMount()) {
            return getGlobalMapping();
        }
        // 获取其顶级映射
        String mntRootId = obj.mountRootId();
        String mount = obj.mount();

        // 自己就是顶端映射对象
        if (Strings.isBlank(mntRootId) || obj.isSameId(mntRootId)) {
            return __check_mapping(obj, mount);
        }

        // 返回
        return checkMapping(mntRootId, mount);
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

    public void setGlobalIndexer(WnIoIndexer globalIndexer) {
        this.globalIndexer = globalIndexer;
    }

    public void setGlobalBM(WnIoBM globalBM) {
        this.globalBM = globalBM;
    }

    public void setIndexers(HashMap<String, WnIndexerFactory> indexers) {
        this.indexers = indexers;
    }

    public void setBms(HashMap<String, WnBMFactory> bms) {
        this.bms = bms;
    }

}
