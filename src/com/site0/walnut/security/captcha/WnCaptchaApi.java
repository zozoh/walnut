package com.site0.walnut.security.captcha;

/**
 * 验证码服务的统一入口，是业务逻辑面对的高级接口。
 *
 * <p>
 * 实例由 {@link WnCaptchaBuilder} 按配置名构建，业务代码不直接接触底层引擎。
 */
public interface WnCaptchaApi {

    /**
     * 生成一份验证码，标准答案由实现临时保存（带过期）
     *
     * @return 验证码载体，{@link WnCaptchaObj#toString()} 即为吐给客户端的 JSON
     */
    WnCaptchaObj generate();

    /**
     * 校验用户提交的行为数据，通过即作废该验证码（一发一验）
     *
     * @param id
     *            验证码的唯一标识（客户端原样带回）
     * @param data
     *            用户行为的采集结果（JSON 字符串，语义由具体实现解释）
     * @return 是否验证通过
     */
    boolean check(String id, String data);

    /**
     * 校验通过后，签发一张绑定账号的临时票据（一次性，带过期）
     *
     * @param account
     *            票据绑定的账号
     * @return 票据标识，回给客户端
     */
    String createTicket(String account);

    /**
     * 业务提交时校验票据：取出映射、比对账号，验过即作废
     *
     * @param ticket
     *            票据标识
     * @param account
     *            业务提交方声称的账号
     * @return true 表示票据有效且账号匹配
     */
    boolean checkTicket(String ticket, String account);

}
