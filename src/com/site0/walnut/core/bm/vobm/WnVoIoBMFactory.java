package com.site0.walnut.core.bm.vobm;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.core.WnIoBM;
import com.site0.walnut.core.WnIoHandleManager;
import com.site0.walnut.core.mapping.WnBMFactory;

public class WnVoIoBMFactory implements WnBMFactory {

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

    public void setSwapPath(String swapPath) {
        this.swapPath = swapPath;
    }

    @Override
    public WnIoBM load(WnObj oHome, String str) {
        // TODO Auto-generated method stub
        return null;
    }

}
