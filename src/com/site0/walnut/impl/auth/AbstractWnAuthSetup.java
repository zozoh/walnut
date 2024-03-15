package com.site0.walnut.impl.auth;

import com.site0.walnut.api.auth.WnAuthSetup;
import com.site0.walnut.api.auth.WnCaptchaService;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;

public abstract class AbstractWnAuthSetup implements WnAuthSetup {

    protected WnIo io;

    protected WnCaptchaService captcha;

    private WnObj oAccountDir;

    private WnObj oSessionDir;

    public AbstractWnAuthSetup(WnIo io) {
        this.io = io;
    }

    protected abstract WnObj getWeixinConf(String codeType);

    protected abstract WnObj createOrFetchAccountDir();

    protected abstract WnObj createOrFetchSessionDir();

    protected abstract WnObj createOrFetchCaptchaDir();

    @Override
    public WnObj getAccountDir() {
        if (null == oAccountDir) {
            synchronized (WnAuthSetup.class) {
                if (null == oAccountDir) {
                    oAccountDir = createOrFetchAccountDir();
                }
            }
        }
        return oAccountDir;
    }

    @Override
    public WnObj getSessionDir() {
        if (null == oSessionDir) {
            synchronized (WnAuthSetup.class) {
                if (null == oSessionDir) {
                    oSessionDir = createOrFetchSessionDir();
                }
            }
        }
        return oSessionDir;
    }

    @Override
    public WnCaptchaService getCaptchaService() {
        if (null == captcha) {
            synchronized (WnAuthSetup.class) {
                if (null == captcha) {
                    WnObj oCapDir = createOrFetchCaptchaDir();
                    captcha = new WnCaptchaServiceImpl(io, oCapDir);
                }
            }
        }
        return captcha;
    }

}