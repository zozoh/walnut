package com.site0.walnut.core;

import java.util.Set;

public interface WnReferApi {

    /**
     * 增加一个从被参考对象(refer)到目标对象(target)的引用。<br>
     * 这个函数是幂等的，即参考对象多次引用目标，实际上也只有一次记录
     * 
     * @param targetId
     *            目标对象ID
     * @param referIds
     *            参考对象ID列表
     * 
     * @return 目标对象总共被引用的数量
     */
    long add(String targetId, String... referIds);

    /**
     * 减少一个从被参考对象(refer)到目标对象(target)的引用。<br>
     * 这个函数是幂等的，即参考对象多次取消对目标的引用，实际上也只有一次有效
     * 
     * @param targetId
     *            目标对象ID
     * @param referIds
     *            参考对象ID列表
     * 
     * @return 目标对象总共被引用的数量
     */
    long remove(String targetId, String... referIds);

    /**
     * @param targetId
     *            目标对象ID
     * @return 目标对象总共被引用的数量
     */
    long count(String targetId);

    /**
     * 获取引用目标对象的全部参考对象ID列表
     * 
     * @param targetId
     *            目标对象 ID
     * @return 参考对象 ID 列表
     */
    Set<String> all(String targetId);

    /**
     * 清除目标对象所有的引用
     * 
     * @param targetId
     *            目标对象 ID
     */
    void clear(String targetId);

}
