package org.nutz.walnut.ext.net.oauth.hdl;

import java.io.ByteArrayOutputStream;
import java.util.Map;

import org.brickred.socialauth.AuthProvider;
import org.brickred.socialauth.Profile;
import org.brickred.socialauth.SocialAuthManager;
import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public class oauth_callback implements JvmHdl {

    @SuppressWarnings("unchecked")
    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        WnObj tmp = sys.io.check(null, Wn.normalizeFullPath("~/.oauth/tmp/" + sys.session.getId(), sys));
        ByteArrayOutputStream ops = new ByteArrayOutputStream();
        sys.io.readAndClose(tmp, ops);
        SocialAuthManager manager = Lang.fromBytes(ops.toByteArray(), SocialAuthManager.class);
        try {
            Map<String, String> params = (Map<String, String>) Json.fromJson(Map.class, sys.in.getReader());
            AuthProvider provider = manager.connect(params);
            Profile p = provider.getUserProfile();
            NutMap re = new NutMap();
            
            re.put("provider", p.getProviderId());
            re.put("profileId", p.getValidatedId());
            re.put("headimgurl", p.getProfileImageURL());
            re.put("aa",p.getDisplayName());
            re.put("country", p.getCountry());
            re.put("gender", p.getGender());
            sys.out.writeJson(re);
        }
        catch (Exception e) {
            sys.err.print("e.oauth.callback.invaild : " + e.getMessage());
            return;
        }
    }
}
