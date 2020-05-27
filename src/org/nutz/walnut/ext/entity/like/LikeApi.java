package org.nutz.walnut.ext.entity.like;

import java.util.Set;

/**
 * 点赞接口
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public interface LikeApi {

    /**
     * 赞它一个
     * 
     * @param taId
     *            被赞主体 ID
     * @param uids
     *            用户们 ID
     * 
     * @return 真正赞赏成功的人数
     */
    long likeIt(String taId, String... uids);

    /**
     * 不再赞它
     * 
     * @param taId
     *            被赞主体 ID
     * @param uids
     *            用户们 ID
     * 
     * @return 真正取消赞赏的人数
     */
    long unlike(String taId, String... uids);

    /**
     * 获取赞赏数量
     * 
     * @param taId
     *            被赞主体 ID
     * @return 赞赏人们的 ID
     */
    Set<String> getAll(String taId);

    /**
     * 获取赞赏数量
     * 
     * @param taId
     *            被赞主体 ID
     * @return 赞赏人们的数量
     */
    long summary(String taId);

    /**
     * 判断一个用户对某赞赏主体是否赞
     * 
     * @param taId
     *            被赞主体 ID
     * @param uid
     *            用户 ID
     * 
     * @return 是否赞
     */
    boolean isLike(String taId, String uid);

}
