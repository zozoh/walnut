package com.site0.walnut.impl.auth;

import com.site0.walnut.api.auth.WnAccount;
import com.site0.walnut.api.auth.WnAuthEventGenerator;
import com.site0.walnut.api.auth.WnAuthSession;
import com.site0.walnut.api.auth.WnAuthEvent;

public class DomainAuthEventGenerator implements WnAuthEventGenerator {

    private String domainName;

    private String domainHomePath;

    public DomainAuthEventGenerator(String domainName, String domainHomePath) {
        this.domainName = domainName;
        this.domainHomePath = domainHomePath;
    }

    @Override
    public WnAuthEvent create(String name, WnAccount account, WnAuthSession session) {
        WnAuthEvent ev = new WnAuthEvent();
        ev.setName(name);
        ev.setDomainName(domainName);
        ev.setDomainHomePath(domainHomePath);
        ev.setMe(account);
        ev.setSession(session);
        return ev;
    }

}
