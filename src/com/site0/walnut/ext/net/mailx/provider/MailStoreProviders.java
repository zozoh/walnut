package com.site0.walnut.ext.net.mailx.provider;

import java.util.HashMap;
import java.util.Map;

import org.nutz.lang.born.Borning;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Ws;

import org.nutz.lang.Mirror;

public class MailStoreProviders {

    private static MailStoreProviders _me = new MailStoreProviders();

    public static MailStoreProviders me() {
        return _me;
    }

    private Map<String, Borning<? extends MailStoreProvider>> borns;

    MailStoreProviders() {
        borns = new HashMap<>();
        this.addBorn("office365", Office365StoreProvider.class);
        this.addBorn("163", Mail163StoreProvider.class);
    }

    private <T extends MailStoreProvider> void addBorn(String name, Class<T> classOfT) {
        Borning<T> born = Mirror.me(classOfT).getBorningByArgTypes(WnSystem.class);
        borns.put(name, born);
    }

    public MailStoreProvider createProvider(WnSystem sys, String name) {
        if (Ws.isBlank(name)) {
            return createDefaultProvider(sys);
        }
        Borning<? extends MailStoreProvider> provide = borns.get(name);
        return provide.born(sys);
    }

    public MailStoreProvider createDefaultProvider(WnSystem sys) {
        return new Mail163StoreProvider(sys);
    }
}
