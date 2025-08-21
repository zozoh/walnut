package com.site0.walnut.core.indexer.vofs;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.site0.walnut.api.err.Er;

public class WnVofsOptions {

    private final static Pattern _P = Pattern.compile("^(s3|cos|oss|obs|kodo):([^#]+)#(.+)$");

    public String osType;
    public String domainHomePath;
    public String configName;

    public WnVofsOptions(String str) {
        Matcher m = _P.matcher(str);
        if (!m.find()) {
            throw Er.create("e.vofs.InvalidSetting", str);
        }
        osType = m.group(1);
        domainHomePath = m.group(2).trim();
        configName = m.group(3).trim();
    }

}
