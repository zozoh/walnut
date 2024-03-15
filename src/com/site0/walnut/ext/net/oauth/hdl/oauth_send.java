package com.site0.walnut.ext.net.oauth.hdl;

import java.io.ByteArrayInputStream;

import org.brickred.socialauth.SocialAuthConfig;
import org.brickred.socialauth.SocialAuthManager;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;

public class oauth_send implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        WnObj conf = sys.io.check(null, Wn.normalizeFullPath("~/.oauth/conf.properties", sys));
        String provider = hc.params.val_check(0);
        String str = sys.io.readText(conf);
        String returnTo = hc.params.val_check(1);
        SocialAuthConfig config = new SocialAuthConfig();
        try {
            config.load(new ByteArrayInputStream(str.getBytes()));
            SocialAuthManager manager = new SocialAuthManager(); // 每次都要新建哦
            manager.setSocialAuthConfig(config);
            String url = manager.getAuthenticationUrl(provider, returnTo);
            String aph = Wn.normalizeFullPath("~/.oauth/tmp/" + sys.session.getId(), sys);
            WnObj tmp = sys.io.createIfNoExists(null, aph, WnRace.FILE);
            sys.io.writeAndClose(tmp, new ByteArrayInputStream(Wlang.toBytes(manager)));
            sys.out.print(url);
        }
        catch (Exception e) {
            sys.err.print("e.oauth.config.invaild : " + e.getMessage());
            return;
        }
    }
}
