package com.site0.walnut.ext.media.mediax.impl;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.media.mediax.bean.MxAccount;
import com.site0.walnut.util.Wn;

/**
 * 基于 Walnut 的文件系统实现
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnMediaXService extends AbstractMediaXService {

    private WnIo io;

    private WnObj oHome;

    public WnMediaXService(WnIo io, WnObj oHome) {
        super();
        this.io = io;
        this.oHome = oHome;
    }

    protected MxAccount _gen_account(String apiKey, String account) {
        MxAccount ac;
        String rph = Wn.appendPath(apiKey, account);
        WnObj oAcc = io.fetch(oHome, rph);
        ac = io.readJson(oAcc, MxAccount.class);
        return ac;
    }

}
