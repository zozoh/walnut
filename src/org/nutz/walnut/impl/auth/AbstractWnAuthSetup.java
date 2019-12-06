package org.nutz.walnut.impl.auth;

import org.nutz.mvc.Mvcs;
import org.nutz.walnut.api.auth.WnAuthSetup;
import org.nutz.walnut.api.auth.WnCaptchaService;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.weixin.WnIoWeixinApi;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.web.WnConfig;

public abstract class AbstractWnAuthSetup implements WnAuthSetup {

    protected WnIo io;

    protected WnCaptchaService captcha;

    protected WnIoWeixinApi weixinApi;

    private WnObj oAccountDir;

    private WnObj oSessionDir;

    public AbstractWnAuthSetup(WnIo io) {
        this.io = io;
    }

    protected abstract WnObj getWeixinConf();

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
    public WnIoWeixinApi getWeixinApi() {
        if (null == weixinApi) {
            synchronized (WnAuthSetup.class) {
                if (null == weixinApi) {
                    WnObj oWxConf = getWeixinConf();
                    if (null != oWxConf) {
                        weixinApi = new WnIoWeixinApi(io, oWxConf);
                    }
                }
            }
        }
        return weixinApi;
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

    @Override
    public long getSessionTransientDuration() {
        return this.getConfLong("se-tmp-du", 60);
    }

    protected long getConfLong(String key, long dft) {
        WnConfig conf = Mvcs.getIoc().get(WnConfig.class, "conf");
        return conf.getLong("key", dft);
    }
}