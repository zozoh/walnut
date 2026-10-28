package com.site0.walnut.security.captcha.impl.tianai;

import org.nutz.json.Json;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.security.captcha.WnCaptchaApi;
import com.site0.walnut.security.captcha.WnCaptchaAnswerStore;
import com.site0.walnut.security.captcha.WnCaptchaObj;
import com.site0.walnut.security.captcha.WnCaptchaTicketStore;
import com.site0.walnut.security.captcha.bean.WnCaptchaConfig;
import com.site0.walnut.util.Ws;

import cloud.tianai.captcha.application.ImageCaptchaApplication;
import cloud.tianai.captcha.application.vo.ImageCaptchaVO;
import cloud.tianai.captcha.common.constant.CaptchaTypeConstant;
import cloud.tianai.captcha.common.response.ApiResponse;
import cloud.tianai.captcha.generator.common.model.dto.GenerateParam;
import cloud.tianai.captcha.generator.common.model.dto.ParamKeyEnum;
import cloud.tianai.captcha.validator.common.model.dto.ImageCaptchaTrack;
import cloud.tianai.captcha.validator.common.model.dto.MatchParam;

/**
 * tianai 版 {@link WnCaptchaApi} 实现：包装 tianai 引擎
 * {@link ImageCaptchaApplication} 与 {@link WnCaptchaTicketStore}。
 *
 * <p>
 * 配置尺度的翻译：容错档位 1-10 线性换算为 tianai 的 0~1 容错值
 * （<code>tolerant * 0.005</code>，即 0.5%~5%）。
 */
public class WnTianaiCaptchaApi implements WnCaptchaApi {

    private ImageCaptchaApplication engine;

    private WnCaptchaConfig conf;

    private WnRedisCaptchaAnswerStore answerStore;

    private WnCaptchaTicketStore ticketStore;

    public WnTianaiCaptchaApi(ImageCaptchaApplication engine,
                              WnCaptchaConfig conf,
                              WnRedisCaptchaAnswerStore answerStore,
                              WnCaptchaTicketStore ticketStore) {
        this.engine = engine;
        this.conf = conf;
        this.answerStore = answerStore;
        this.ticketStore = ticketStore;
    }

    @Override
    public WnCaptchaObj generate() {
        // 文字点选必须有字体
        if (CaptchaTypeConstant.WORD_IMAGE_CLICK.equals(conf.getType()) && Ws.isBlank(conf.getFontPath())) {
            throw Er.create("e.captcha.NoFont", conf.getType());
        }
        // 生成参数：抽象配置翻译成 tianai 的语言
        GenerateParam param = new GenerateParam();
        param.setType(conf.getType());
        param.setObfuscate(conf.isObfuscate());
        param.addParam(ParamKeyEnum.TOLERANT, conf.getTolerant() * 0.005f);
        // 生成
        ApiResponse<ImageCaptchaVO> re = engine.generateCaptcha(param);
        if (!re.isSuccess()) {
            throw Er.create("e.captcha.GenerateFail", re.getMsg());
        }
        return new WnTianaiCaptchaObj(re.getData());
    }

    @Override
    public boolean check(String id, String data) {
        if (Ws.isBlank(id) || Ws.isBlank(data)) {
            return false;
        }
        // 客户端提交的轨迹 JSON → tianai 轨迹对象
        ImageCaptchaTrack track = Json.fromJson(ImageCaptchaTrack.class, data);
        if (null == track) {
            return false;
        }
        // matching 通过即作废（一发一验由引擎的缓存保证）
        ApiResponse<?> re = engine.matching(id, new MatchParam(track));
        return re.isSuccess();
    }

    @Override
    public String createTicket(String account) {
        return ticketStore.createTicket(account, conf.getTicketExpire());
    }

    @Override
    public boolean checkTicket(String ticket, String account) {
        return ticketStore.checkTicket(ticket, account);
    }

    /**
     * @return 答案存取接口（业务侧的手工存取场景用，引擎内部校验已经走它）
     */
    public WnCaptchaAnswerStore getAnswerStore() {
        return answerStore;
    }

}
