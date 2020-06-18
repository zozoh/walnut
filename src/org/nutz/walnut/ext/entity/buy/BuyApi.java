package org.nutz.walnut.ext.entity.buy;

import java.util.List;

/**
 * 购物车接口
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public interface BuyApi {
    /**
     * 修改购买数量
     * 
     * @param uid
     *            用户 ID
     * @param taId
     *            商品 ID
     * @param count
     *            购买数量
     * @param reset
     *            true: 购买数量为重置模式； false: 购买数量为增量模式
     * 
     * @return 总购买数量
     */
    int buyIt(String uid, String taId, int count, boolean reset);

    /**
     * 移除一个或多个商品
     * 
     * @param uid
     *            用户 ID
     * @param taIds
     *            商品 ID 列表
     * @return 真实被移除的条目数量
     */
    int remove(String uid, String... taIds);

    /**
     * 清空购物车
     * 
     * @param uid
     *            用户 ID
     */
    boolean clean(String uid);

    /**
     * 获得购物车全部商品列表
     * 
     * @param uid
     *            用户 ID
     * @return 购买商品列表
     */
    List<BuyIt> getAll(String uid);

    /**
     * 获得购物车全部商品列表（反序）
     * 
     * @param uid
     *            用户 ID
     * @return 购买商品列表
     */
    List<BuyIt> revAll(String uid);

    /**
     * 获取购买商品品类数量
     * 
     * @param uid
     *            用户 ID
     * @return 品类数量
     */
    int count(String uid);

    /**
     * 获取购买商品数量
     * 
     * @param uid
     *            用户 ID
     * @return 数量
     */
    int sum(String uid);

    /**
     * 判断一个用户对某主体打的具体分数
     * 
     * @param uid
     *            用户 ID
     * @param taId
     *            商品 ID
     * @param dft
     *            如果记录，返回的默认值
     * 
     * @return 购买数量
     */
    int getBuy(String taId, String uid, int dft);
}
