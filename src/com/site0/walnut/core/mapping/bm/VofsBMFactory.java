package com.site0.walnut.core.mapping.bm;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.core.WnIoBM;
import com.site0.walnut.core.WnIoHandleManager;
import com.site0.walnut.core.bm.vofs.VofsBM;
import com.site0.walnut.core.mapping.WnBMFactory;
import com.site0.walnut.core.mapping.support.WnVofsOptions;
import com.site0.walnut.ext.xo.impl.XoService;

public class VofsBMFactory implements WnBMFactory {

    /**
     * 这个需要通过 IOC 注入得到实例
     */
    private WnIo io;
    private WnIoHandleManager handles;
    private String swapPath;

    public void setIo(WnIo io) {
        this.io = io;
    }

    public void setHandles(WnIoHandleManager handles) {
        this.handles = handles;
    }

    @Override
    public WnIoBM load(WnObj oHome, String str) {
        WnVofsOptions opt = new WnVofsOptions(str);

        // 获取配置主目录
        WnObj oXHome = io.check(null, opt.domainHomePath);

        XoService xos = opt.buildXoService(io, oXHome);

        return new VofsBM(handles, swapPath, xos);
    }

}
