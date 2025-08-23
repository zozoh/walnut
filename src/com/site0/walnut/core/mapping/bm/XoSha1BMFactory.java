package com.site0.walnut.core.mapping.bm;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.core.WnIoBM;
import com.site0.walnut.core.WnIoHandleManager;
import com.site0.walnut.core.WnReferApi;
import com.site0.walnut.core.bm.bmv.XoSha1BM;
import com.site0.walnut.core.mapping.WnBMFactory;
import com.site0.walnut.core.mapping.support.WnVoBMOptions;
import com.site0.walnut.ext.xo.impl.XoService;
import com.site0.walnut.web.WnConfig;

public class XoSha1BMFactory implements WnBMFactory {

    /**
     * 这个需要通过 IOC 注入得到实例
     */
    private WnConfig conf;
    private WnIo io;
    private WnIoHandleManager handles;
    private String swapPath;
    private boolean autoCreateSwap;

    public void setIo(WnIo io) {
        this.io = io;
    }

    public void setHandles(WnIoHandleManager handles) {
        this.handles = handles;
    }

    public void setSwapPath(String swapPath) {
        this.swapPath = swapPath;
    }

    @Override
    public WnIoBM load(WnObj oHome, String str) {
        WnVoBMOptions opt = new WnVoBMOptions(str);

        // 获取配置主目录
        WnObj oXHome = io.check(null, opt.domainHomePath);

        XoService xos = opt.buildXoService(io, oXHome);
        WnReferApi refers = opt.getReferApi(io, conf);

        return new XoSha1BM(handles,
                            swapPath,
                            autoCreateSwap,
                            opt.signAlg,
                            opt.parts,
                            xos,
                            refers);
    }

}
