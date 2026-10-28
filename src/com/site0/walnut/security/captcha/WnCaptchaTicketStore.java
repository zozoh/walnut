package com.site0.walnut.security.captcha;

/**
 * 人机校验临时票据的存取接口。
 *
 * 人机校验通过后，业务动作真正执行前往往还隔着几次交互（如回填 OTP）。 票据把「校验已通过」这个结论绑定到具体账号上： 签发时写入
 * ticket → account 映射，业务提交时校验并作废。
 *
 * 与答案不同，票据在业务提交时才被消费，那次请求可能落到集群里 的另一个节点，所以票据的实现类必须是多节点共享的存储（如 Redis）。
 */
public interface WnCaptchaTicketStore {

    /**
     * 签发一张临时票据
     *
     * @param account
     *            票据绑定的账号
     * @param expire
     *            过期时间（毫秒），到点即作废
     * @return 新生成的票据标识
     */
    String createTicket(String account, long expire);

    /**
     * 校验一张票据：取出映射、比对账号，验过即作废（一次性）
     *
     * @param ticket
     *            票据标识
     * @param account
     *            业务提交方声称的账号
     * @return true 表示票据有效且账号匹配
     */
    boolean checkTicket(String ticket, String account);

}
