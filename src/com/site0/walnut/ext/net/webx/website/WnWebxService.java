package com.site0.walnut.ext.net.webx.website;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.ext.net.webx.website.loader.UniqWebxAccountLoader;
import com.site0.walnut.ext.net.webx.website.loader.UniqWebxRoleLoader;

public class WnWebxService {

    private WnWebx web;

    private WnIo io;

    private NutBean sessionVars;

    public WnWebxService(WnSystem sys) {
        this.web = new WnWebx();
        this.io = sys.io;
        this.sessionVars = sys.session.getVars();
    }

    public void loadWebsite(String ph) {
        String aph = Wn.normalizeFullPath(ph, sessionVars);
        WnObj oHome = io.check(null, aph);
        NutBean info = oHome;

        // 文件的话，需要读取内容，将 JSON 内容作为设置信息
        if (oHome.isFILE()) {
            info = io.readJson(oHome, NutMap.class);
        }

        // 读取设置信息
        String accountHomePath = info.getString("accounts");
        String roleHomePath = info.getString("roles");
        NutBean dftEnv = info.getAs("env", NutMap.class);

        // 设置账号加载器
        if (!Ws.isBlank(accountHomePath)) {
            web.setAccounts(new UniqWebxAccountLoader(io, sessionVars, accountHomePath, dftEnv));
        }

        // 设置角色加载器
        if (!Ws.isBlank(roleHomePath)) {
            web.setRoles(new UniqWebxRoleLoader(io, sessionVars, roleHomePath));
        }
    }

}
