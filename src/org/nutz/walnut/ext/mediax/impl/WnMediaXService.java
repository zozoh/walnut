package org.nutz.walnut.ext.mediax.impl;

import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.mediax.bean.MxAccount;
import org.nutz.walnut.util.Wn;

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
