package com.site0.walnut.ext.xo.util;

import java.util.HashMap;
import java.util.Map;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.xo.builder.XoClientBuilder;
import com.site0.walnut.ext.xo.provider.XoClientProvider;

public class XoClientManager<T> {

    private XoClientProvider<T> provider;

    private Map<String, XoClientWrapper<T>> clients;

    XoClientManager(XoClientProvider<T> provider) {
        this.provider = provider;
        this.clients = new HashMap<>();
    }

    public XoClientWrapper<T> getClient(WnIo io, WnObj oHome, String name) {
        String key = XoClients.genClientKey(oHome, name);
        XoClientWrapper<T> re = clients.get(key);
        long now = System.currentTimeMillis();
        if (null == re || re.isExpired(now)) {
            // 再次获取
            synchronized (clients) {
                re = clients.get(name);
                if (null == re || re.isExpired(now)) {
                    // 过期了，就关闭
                    if (null != re) {
                        re.close();
                        clients.remove(key);
                    }
                    try {
                        XoClientBuilder<T> ing = provider.getBuilder(io, oHome, name);
                        re = ing.build();
                        clients.put(key, re);
                    }
                    catch (Exception e) {
                        throw Er.wrap(e);
                    }

                }
            }
        }
        return re;
    }

}
