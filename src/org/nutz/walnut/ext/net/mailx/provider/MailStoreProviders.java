package org.nutz.walnut.ext.net.mailx.provider;

import java.util.HashMap;
import java.util.Map;

import org.nutz.lang.born.Borning;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wlang;
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
    }

    private <T extends MailStoreProvider> void addBorn(String name, Class<T> classOfT) {
        Borning<T> born = Mirror.me(classOfT).getBorningByArgTypes(WnSystem.class);
        borns.put(name, born);
    }

    public MailStoreProvider createProvider(WnSystem sys, String name) {
        Borning<? extends MailStoreProvider> provide = borns.get(name);
        return provide.born(sys);
    }

    public MailStoreProvider createDefaultProvider(WnSystem sys) {
        throw Wlang.noImplement();
    }
}
