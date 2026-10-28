package com.site0.walnut.security.captcha;

/**
 * 验证码答案的临时存取接口。
 *
 * 生成验证码时把标准答案临时存进来（带过期），校验时取出比对并作废。 答案由生成它的节点在校验时消费，实现类可以是节点本地缓存，
 * 也可以是 Redis 等共享存储。
 */
public interface WnCaptchaAnswerStore {

    /**
     * 保存一个验证码答案
     *
     * @param id
     *            验证码的唯一标识
     * @param answer
     *            标准答案的序列化形式（语义由具体实现解释）
     * @param expire
     *            过期时间（毫秒），到点即作废
     */
    void save(String id, String answer, long expire);

    /**
     * 取出一个验证码答案（不删除）
     *
     * @param id
     *            验证码的唯一标识
     * @return 标准答案，不存在或已过期返回 null
     */
    String get(String id);

    /**
     * 删除一个验证码答案（校验通过或作废时调用，保证一发一验）
     *
     * @param id
     *            验证码的唯一标识
     */
    void remove(String id);

}
