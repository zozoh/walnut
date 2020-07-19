package org.nutz.walnut.core;

import java.util.List;

public interface WnReferApi {

    /**
     * 增加一个从被参考对象(refer)到目标对象(target)的引用。<br>
     * 这个函数是幂等的，即参考对象多次引用目标，实际上也只有一次记录
     * 
     * @param targetId
     *            目标对象ID
     * @param referId
     *            参考对象ID
     * 
     * @return 目标对象总共被引用的数量
     */
    long add(String targetId, String referId);

    /**
     * 减少一个从被参考对象(refer)到目标对象(target)的引用。<br>
     * 这个函数是幂等的，即参考对象多次取消对目标的引用，实际上也只有一次有效
     * 
     * @param targetId
     *            目标对象ID
     * @param referId
     *            参考对象ID
     * 
     * @return 目标对象总共被引用的数量
     */
    long remove(String targetId, String referId);

    /**
     * @param targetId
     *            目标对象ID
     * @return 目标对象总共被引用的数量
     */
    long count(String targetId);

    /**
     * 获取引用目标对象的参考对象ID列表
     * 
     * @param targetId
     *            目标对象 ID
     * @param limit
     *            最多查询多少个结果
     * @param skip
     *            跳过多少个结果开始查询
     * @return 参考对象 ID 列表
     */
    List<String> scan(String targetId, long limit, long skip);

    /**
     * 清除目标对象所有的引用
     * 
     * @param targetId
     *            目标对象 ID
     * @return 参考对象 ID 列表
     */
    List<String> clear(String targetId);

}
