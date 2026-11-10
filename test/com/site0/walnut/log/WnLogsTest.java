package com.site0.walnut.log;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

/**
 * RM-NUTZ-11 日志封装冒烟：确认 {@link WnLogs} 入口可用且 {@code infof} 可打印。
 */
public class WnLogsTest {

    @Test
    public void smoke() {
        WnLog main = WnLogs.getMain();
        assertNotNull(main);
        assertNotNull(WnLogs.getApp());
        assertNotNull(WnLogs.getCmd());
        main.infof("WnLogs smoke %s", "ok");
        WnLogs.get("TEST").info("WnLogs smoke info");
        WnLogs.getCmd().debugf("WnLogs smoke debug %d", 1);
    }

}
