package org.nutz.walnut.impl.auth;

import org.nutz.walnut.api.auth.WnAuthService;
import org.nutz.walnut.api.auth.WnAuthSession;

public class WnAuthServiceImpl implements WnAuthService {

    @Override
    public String getDefaultRoleName() {
        return null;
    }

    @Override
    public WnAuthSession getSession(String ticket) {
        return null;
    }

    @Override
    public WnAuthSession checkSession(String ticket) {
        return null;
    }

    @Override
    public WnAuthSession removeSession(String ticket) {
        return null;
    }

    @Override
    public WnAuthSession loginByWxCode(String code) {
        return null;
    }

    @Override
    public WnAuthSession bindAccount(String account, String scene, String vcode, String ticket) {
        return null;
    }

    @Override
    public WnAuthSession loginByVcode(String account, String scene, String vcode) {
        return null;
    }

    @Override
    public WnAuthSession loginByPasswd(String account, String passwd) {
        return null;
    }

    @Override
    public WnAuthSession logout(String ticket) {
        return null;
    }

}
