package com.site0.walnut.impl.auth.account;

import com.site0.walnut.api.auth.WnAccount;
import com.site0.walnut.api.auth.WnAccountLoader;
import com.site0.walnut.api.err.Er;

public abstract class AbstractAccountLoader implements WnAccountLoader {

    @Override
    public WnAccount getAccount(String nameOrPhoneOrEmail) {
        WnAccount info = new WnAccount(nameOrPhoneOrEmail);
        return this.getAccount(info);
    }

    @Override
    public WnAccount checkAccount(String nameOrPhoneOrEmail) {
        WnAccount u = this.getAccount(nameOrPhoneOrEmail);
        if (null == u) {
            throw Er.create("e.auth.account.noexists", nameOrPhoneOrEmail);
        }
        return u;
    }

    @Override
    public WnAccount checkAccount(WnAccount info) {
        WnAccount u = this.getAccount(info);
        if (null == u) {
            throw Er.create("e.auth.account.noexists", info.toBean());
        }
        return u;
    }

    @Override
    public WnAccount checkAccountById(String uid) {
        WnAccount u = this.getAccountById(uid);
        if (null == u) {
            throw Er.create("e.auth.account.noexists", uid);
        }
        return u;
    }

}
