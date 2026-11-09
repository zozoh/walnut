package com.site0.walnut.web2.support;

import com.site0.walnut.api.io.WnObj;

/**
 * 域名路由记录读取（包装 {@code WnIo} 的 <code>/domain/{host}</code> 查询）。
 */
public interface WnDomainResolver {

    /**
     * 按 host 取域名记录；不存在返回 <code>null</code>。
     */
    WnObj fetchDomain(String host);

}
