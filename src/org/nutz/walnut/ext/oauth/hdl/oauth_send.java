package org.nutz.walnut.ext.oauth.hdl;

import java.io.ByteArrayInputStream;

import org.brickred.socialauth.SocialAuthConfig;
import org.brickred.socialauth.SocialAuthManager;
import org.nutz.lang.Lang;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

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
            WnObj tmp = sys.io.createIfNoExists(null, Wn.normalizeFullPath("~/.oauth/tmp/" + sys.se.id(), sys), WnRace.FILE);
            sys.io.writeAndClose(tmp, new ByteArrayInputStream(Lang.toBytes(manager)));
            sys.out.print(url);
        }
        catch (Exception e) {
            sys.err.print("e.oauth.config.invaild : " + e.getMessage());
            return;
        }
    }
}
