package org.nutz.walnut.ext.entity.favor;

import java.util.List;

/**
 * 收藏接口
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public interface FavorApi {

    /**
     * 收藏一个主体
     * 
     * @param uid
     *            用户 ID
     * @param taIds
     *            被收藏主体 ID
     * 
     * @return 收藏成功的主体数
     */
    long favorIt(String uid, String... taIds);

    /**
     * 不再收藏一个主体
     * 
     * @param uid
     *            用户 ID
     * @param taIds
     *            被收藏主体 ID
     * 
     * @return 真正取消收藏的主体数
     */
    long unfavor(String uid, String... taIds);

    /**
     * 获取收藏主体ID列表
     * 
     * @param uid
     *            用户 ID
     * @param limit
     *            最多返回的数量，0 表示不限制
     * @return 收藏主体列表
     */
    List<FavorIt> getAll(String uid, int limit);

    /**
     * 获取收藏主体ID数量
     * 
     * @param uid
     *            用户 ID
     * @return 赞赏人们的数量
     */
    long summary(String uid);

    /**
     * 判断一个用户对某主体是否已经收藏
     * 
     * @param uid
     *            用户 ID
     * 
     * @param taId
     *            被收藏主体
     * 
     * @return 具体收藏的时间（绝对毫秒数）
     */
    long whenFavor(String uid, String taId);

}
