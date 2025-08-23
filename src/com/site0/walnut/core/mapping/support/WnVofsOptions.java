package com.site0.walnut.core.mapping.support;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.xo.impl.CosXoService;
import com.site0.walnut.ext.xo.impl.S3XoService;
import com.site0.walnut.ext.xo.impl.XoService;

public class WnVofsOptions {

    private final static Pattern _P = Pattern
        .compile("^(s3|cos|oss|obs|kodo):([^#]+)#(.+)$");

    public String osType;
    public String domainHomePath;
    public String configName;

    WnVofsOptions() {}

    public WnVofsOptions(String str) {
        Matcher m = _P.matcher(str);
        if (!m.find()) {
            throw Er.create("e.vofs.InvalidSetting", str);
        }
        osType = m.group(1);
        domainHomePath = m.group(2).trim();
        configName = m.group(3).trim();
    }

    public String toString() {
        return String.format("%s:%s#%s", osType, domainHomePath, configName);
    }

    public XoService buildXoService(WnIo io, WnObj oXHome) {
        XoService xos = null;
        // S3 亚马逊
        if ("s3".equals(osType)) {
            xos = new S3XoService(io, oXHome, configName);
        }
        // COS 腾讯云
        else if ("cos".equals(osType)) {
            xos = new CosXoService(io, oXHome, configName);
        }
        // OSS 阿里云
        // OBS 华为云
        // KODO 七牛
        else {
            throw Er.create("e.vofs.UnsupportMode", this.toString());
        }
        return xos;
    }

}
