package org.nutz.walnut.ext.entity.score;

import java.util.List;

/**
 * 打分接口
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public interface ScoreApi {

    /**
     * 打分一个主体
     * 
     * @param taId
     *            被打分主体 ID
     * @param uid
     *            用户 ID
     * @param score
     *            分数
     * 
     * @return 总分
     */
    long scoreIt(String taId, String uid, long score);

    /**
     * 取消对一个主体的打分
     * 
     * @param taId
     *            被打分主体 ID
     * @param uid
     *            用户 ID
     * 
     * @return 总分
     */
    long cancel(String taId, String uid);

    /**
     * 获取打分主体列表
     * 
     * @param taId
     *            被打分主体 ID
     * @param skip
     *            跳过多少个被打分主体
     * @param limit
     *            最多返回的数量，0 表示不限制
     * @return 打分主体列表
     */
    List<ScoreIt> getAll(String taId, int skip, int limit);

    /**
     * 获取打分主体列表（反序）
     * 
     * @param taId
     *            被打分主体 ID
     * @param skip
     *            跳过多少个被打分主体
     * @param limit
     *            最多返回的数量，0 表示不限制
     * @return 打分主体列表
     */
    List<ScoreIt> revAll(String taId, int skip, int limit);

    /**
     * 获取打分主体的被打分数量
     * 
     * @param taId
     *            被打分主体 ID
     * @return 被打了多少次分
     */
    long count(String taId);

    /**
     * 获取打分主体的总分
     * 
     * @param taId
     *            被打分主体 ID
     * @return 打分主体的总分
     */
    long sum(String taId);

    /**
     * 重新打分主体的总分（可能会比较慢）
     * 
     * @param taId
     *            被打分主体 ID
     * @return 打分主体的总分
     */
    long resum(String taId);

    /**
     * 判断一个用户对某主体打的具体分数
     * 
     * @param taId
     *            被打分主体 ID
     * @param uid
     *            用户 ID
     * @param dft
     *            如果没有打分，返回的默认分数
     * 
     * @return 具体打分的时间（绝对毫秒数）
     */
    long getScore(String taId, String uid, long dft);

}
