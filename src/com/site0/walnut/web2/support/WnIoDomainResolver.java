package com.site0.walnut.web2.support;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;

/**
 * {@link WnDomainResolver} 的正式实现：从 <code>/domain/{host}</code> 读记录。
 */
public class WnIoDomainResolver implements WnDomainResolver {

    private final WnIo io;

    private volatile WnObj domainHome;

    public WnIoDomainResolver(WnIo io) {
        this.io = io;
    }

    @Override
    public WnObj fetchDomain(String host) {
        if (null == io || null == host || host.isEmpty())
            return null;
        if (null == domainHome) {
            synchronized (this) {
                if (null == domainHome)
                    domainHome = io.createIfNoExists(null, "/domain", WnRace.DIR);
            }
        }
        if (null == domainHome)
            return null;
        return io.fetch(domainHome, host);
    }

}
