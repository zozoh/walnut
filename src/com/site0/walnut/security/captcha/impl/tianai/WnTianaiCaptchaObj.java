package com.site0.walnut.security.captcha.impl.tianai;

import org.nutz.json.Json;

import com.site0.walnut.security.captcha.WnCaptchaObj;

import cloud.tianai.captcha.application.vo.ImageCaptchaVO;

/**
 * 包装 tianai 的 {@link ImageCaptchaVO}：
 * <code>toString()</code> 输出给客户端的 JSON，
 * <code>fromString()</code> 从字符串还原。
 */
public class WnTianaiCaptchaObj implements WnCaptchaObj {

    private ImageCaptchaVO vo;

    public WnTianaiCaptchaObj() {
    }

    public WnTianaiCaptchaObj(ImageCaptchaVO vo) {
        this.vo = vo;
    }

    @Override
    public String getId() {
        return null == vo ? null : vo.getId();
    }

    @Override
    public String toString() {
        return Json.toJson(vo);
    }

    @Override
    public void fromString(String input) {
        this.vo = Json.fromJson(ImageCaptchaVO.class, input);
    }

    /**
     * @return 包装的 tianai 验证码对象
     */
    public ImageCaptchaVO getVo() {
        return vo;
    }

}
