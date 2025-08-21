package com.site0.walnut.core.bm.vofs;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.core.WnIoBM;
import com.site0.walnut.core.WnIoHandleManager;
import com.site0.walnut.core.indexer.vofs.WnVofsOptions;
import com.site0.walnut.core.mapping.WnBMFactory;
import com.site0.walnut.ext.xo.impl.CosXoService;
import com.site0.walnut.ext.xo.impl.S3XoService;
import com.site0.walnut.ext.xo.impl.XoService;

public class VofsBMFactory implements WnBMFactory {

    /**
     * 这个需要通过 IOC 注入得到实例
     */
    private WnIo io;
    private WnIoHandleManager handles;

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

        XoService xos = null;
        // S3 亚马逊
        if ("s3".equals(opt.osType)) {
            xos = new S3XoService(io, oXHome, opt.configName);
        }
        // COS 腾讯云
        else if ("cos".equals(opt.osType)) {
            xos = new CosXoService(io, oXHome, opt.configName);
        }
        // OSS 阿里云
        // OBS 华为云
        // KODO 七牛
        else {
            throw Er.create("e.vofs.UnsupportMode", str);
        }

        return new VofsBM(handles, xos);
    }

}
