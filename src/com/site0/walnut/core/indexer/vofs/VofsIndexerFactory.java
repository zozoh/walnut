package com.site0.walnut.core.indexer.vofs;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.MimeMap;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnIoIndexer;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.core.mapping.WnIndexerFactory;
import com.site0.walnut.ext.xo.impl.CosXoService;
import com.site0.walnut.ext.xo.impl.S3XoService;
import com.site0.walnut.ext.xo.impl.XoService;

public class WnVofsIndexerFactory implements WnIndexerFactory {

    /**
     * 这个需要通过 IOC 注入得到实例
     */
    private WnIo io;
    private MimeMap mimes;

    public void setIo(WnIo io) {
        this.io = io;
    }

    public void setMimes(MimeMap mimes) {
        this.mimes = mimes;
    }

    @Override
    public WnIoIndexer load(WnObj oMntRoot, String str) {
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

        return new WnVofsIndexer(oMntRoot, mimes, xos);
    }

}
