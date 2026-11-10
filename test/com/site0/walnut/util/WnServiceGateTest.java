package com.site0.walnut.util;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;
import org.nutz.web.WebException;

import com.site0.walnut.api.box.WnServiceHolder;

/**
 * RM-NUTZ-11 能力门：上下文内可正常取服务；裸线程 fail-closed（拒绝而不是 null）。
 */
public class WnServiceGateTest {

    @Test
    public void normal_context_services() {
        WnServiceHolder holder = new WnServiceHolder();
        try {
            Wn.bindServices(holder);
            assertSame(holder, Wn.services());
            assertSame(holder, Wn.Service.services());
        }
        finally {
            Wn.Ctx.clear();
        }
    }

    @Test
    public void reject_without_context() throws Exception {
        AtomicReference<Throwable> err = new AtomicReference<>();
        Thread t = new Thread(() -> {
            try {
                Wn.services();
            }
            catch (Throwable e) {
                err.set(e);
            }
        }, "wn-no-context");
        t.start();
        t.join(3000);

        Throwable e = err.get();
        if (null == e) {
            fail("bare thread should be rejected by capability gate");
        }
        assertTrue(e instanceof WebException);
        assertTrue(((WebException) e).getKey().contains("e.wc.null.services"));
    }

}
