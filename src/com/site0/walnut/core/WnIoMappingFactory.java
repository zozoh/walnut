package com.site0.walnut.core;

import com.site0.walnut.api.io.WnIoIndexer;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.core.bean.WnObjMapping;
import com.site0.walnut.core.mapping.MountInfo;

public interface WnIoMappingFactory {

    /**
     * 根据顶端映射对象（逐级查找遇到的第一个声明mnt的对象）获取一个映射实例
     * <p>
     * 当然，在我们的假想中，映射不能再包括映射，<br>
     * 这个实现会导致过于复杂，且没有实际应用场景支撑，不值得为此付出代价。
     * 
     * @param homeId
     *            顶端映射对象ID。如果这个接口被调用，它不应该为 null
     * @param mount
     *            映射声明字符串
     * @return 子映射对象
     */
    WnIoMapping checkMapping(String homeId, String mount);

    /**
     * 根据一个对象，获取其子映射。
     * <p>
     * 本函数是一个帮助函数，会自动获取对象的顶端映射对象ID，以及映射信息，<br>
     * 并通过 {@link #checkMapping(String, String)} 获取映射对象
     * 
     * @param obj
     *            某个对象
     * @return 子映射对象
     */
    WnIoMapping checkMapping(WnObj obj);

    WnIoMapping getGlobalMapping();

    WnIoIndexer getGlobalIndexer();

    WnObj getRoot();

    /**
     * 根据一个两段式ID(<code>xxx:xxx</code>)获取一个映射对象
     * <p>
     * 如果不是两段式，则采用全局的索引管理器和桶管理器<br>
     * 否则，第一段ID必须为映射的HOME对象，且可以由全局索引管理器获得
     * 
     * @param id
     *            两段式 ID
     * @return 映射信息对象（包括ID,obj以及mapping对象的组合）
     */
    WnObjMapping checkById(String id);

    /**
     * 加载一个索引管理器实例
     * 
     * @param oHome
     *            映射主目录
     * @param ixMount
     *            索引管理器映射字符
     * @return 索引管理器
     */
    WnIoIndexer loadIndexer(WnObj oHome, String ixMount);

    /**
     * 加载一个索引管理器实例
     * 
     * @param oHome
     * @param miix
     *            索引管理器映射描述对象
     * @return 索引管理器
     */
    WnIoIndexer loadIndexer(WnObj oHome, MountInfo.Item miix);

    /**
     * 加载一个桶管理器实例
     * 
     * @param oHome
     *            映射主目录
     * @param bmMount
     *            桶管理器映射字符
     * @return 桶管理器
     */
    WnIoBM loadBM(WnObj oHome, String bmMount);

    /**
     * 加载一个桶管理器实例
     * 
     * @param oHome
     * @param mibm
     *            桶管理器映射描述对象
     * @return 桶管理器
     */
    WnIoBM loadBM(WnObj oHome, MountInfo.Item mibm);

}
