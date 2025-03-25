package com.site0.walnut.login.usr;

import org.nutz.lang.util.NutBean;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.login.WnLoginSessionOptions;

public class WnSessionStoreSetup  extends WnLoginSessionOptions{

    public WnIo io;
    public NutBean sessionVars;

    /**
     * 默认环境变量
     */
    public NutBean defaultEnv;
}
