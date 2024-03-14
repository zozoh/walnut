package com.site0.walnut.impl.lock;

import static org.junit.Assert.*;

import org.junit.Test;

public class WnLockObjTest {

    @Test
    public void test_stringify() {
        String lock = "LOCK_TASK_QUEUE=node73:78rr21#push_task";

        WnLockObj lo = WnLockObj.create(lock);
        assertEquals("LOCK_TASK_QUEUE", lo.getName());
        assertEquals("node73", lo.getOwner());
        assertEquals("78rr21", lo.getPrivateKey());
        assertEquals("push_task", lo.getHint());

        WnLockObj l2 = WnLockObj.create("node73:78rr21#push_task");
        assertEquals("node73", lo.getOwner());
        assertEquals("78rr21", lo.getPrivateKey());
        assertEquals("push_task", lo.getHint());
        l2.setName(lo.getName());
        assertTrue(l2.isSame(lo));

        String s3 = lo.toValue();
        assertEquals("node73:78rr21", s3);
        WnLockObj l3 = WnLockObj.create(s3);
        l3.setName(lo.getName());
        assertTrue(l3.isSame(l2));

    }

}
