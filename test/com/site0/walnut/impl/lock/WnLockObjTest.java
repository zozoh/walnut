package com.site0.walnut.impl.lock;

import static org.junit.Assert.*;

import org.junit.Test;

public class WnLockObjTest {

    @Test
    public void test_stringify() {
        String lock = "78rr21;name=LOCK_TASK_QUEUE,owner=node73,hold=,expi=,hint=push_task";

        WnLockObj lo = WnLockObj.create(lock);
        assertEquals("LOCK_TASK_QUEUE", lo.getName());
        assertEquals("node73", lo.getOwner());
        assertEquals("78rr21", lo.getPrivateKey());
        assertEquals(0, lo.getExpiTime());
        assertEquals(0, lo.getHoldTime());
        assertEquals("push_task", lo.getHint());

        WnLockObj l2 = WnLockObj.create(lock);
        assertTrue(l2.isSame(lo));

        String s3 = lo.toString();
        assertEquals(lock, s3);

    }

}
