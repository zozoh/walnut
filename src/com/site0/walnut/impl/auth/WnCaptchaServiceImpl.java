package com.site0.walnut.impl.auth;

import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.auth.WnCaptcha;
import com.site0.walnut.api.auth.WnCaptchaService;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.util.Wn;

public class WnCaptchaServiceImpl implements WnCaptchaService {

    private WnIo io;

    private WnObj oCaptchaHome;

    public WnCaptchaServiceImpl(WnIo io, WnObj oCaptchaHome) {
        this.io = io;
        this.oCaptchaHome = oCaptchaHome;
    }

    /**
     * 保存验证码，如果存在，替换
     * 
     * @param cap
     *            验证码对象
     * @return 验证码的数据对象
     */
    @Override
    public WnObj saveCaptcha(WnCaptcha cap) {
        // 得到场景目录
        String path = Wn.appendPath(cap.getScene(), cap.getAccount());
        WnObj oFile = io.createIfExists(oCaptchaHome, path, WnRace.FILE);

        // 设置
        NutMap meta = cap.toMeta(null);
        io.appendMeta(oFile, meta);

        // 搞定
        return oFile;
    }

    /**
     * 检查如果存在，就移除。如果不能匹配，则 retry+1, 如果匹配就移除并返回true
     * 
     * @param scene
     *            场景
     * @param account
     *            账号
     * @param code
     *            待验证的的验证码
     * @return 是否匹配成功
     */
    @Override
    public boolean removeCaptcha(String scene, String account, String code) {
        String path = Wn.appendPath(scene, account);
        WnObj oFile = io.fetch(oCaptchaHome, path);

        if (null == oFile)
            return false;

        WnCaptcha cap = new WnCaptcha(oFile);

        // 过期，失败
        if (cap.isExpired()) {
            io.delete(oFile);
            return false;
        }

        // 验证码不匹配，重试+1，超过最大次数，删除
        if (!cap.isSameCode(code)) {
            cap.incRetry();
            if (cap.isNoMoreRetry()) {
                io.delete(oFile);
            }
            // 增加一个 retry
            else {
                io.inc(oFile.id(), cap.getRetryKey(), 1, false);
            }
            // 返回失败
            return false;
        }

        // 匹配成功，删除
        io.delete(oFile);
        return true;

    }

}
