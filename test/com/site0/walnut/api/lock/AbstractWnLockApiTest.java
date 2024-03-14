package com.site0.walnut.api.lock;

import static org.junit.Assert.*;

import org.junit.Test;
import org.nutz.lang.Lang;
import org.nutz.trans.Atom;
import com.site0.walnut.core.IoCoreTest;

public abstract class AbstractWnLockApiTest extends IoCoreTest {

    private static final String LO_NM = "TEST_LOCK";
    private static final String LO_OW = "zozoh";
    private static final String LO_HI = "just for test";

    /**
     * 子类需要设置这个参数
     */
    protected WnLockApi locks;

    @Test
    public void test_simple_try_free() throws Exception {
        WnLock lo = locks.tryLock(LO_NM, LO_OW, LO_HI, 1000);
        assertEquals(LO_NM, lo.getName());
        assertEquals(LO_OW, lo.getOwner());
        assertEquals(LO_HI, lo.getHint());
        assertNotNull(lo.getPrivateKey());

        WnLock l2 = locks.getLock(LO_NM);
        assertEquals(LO_NM, l2.getName());
        assertEquals(LO_OW, l2.getOwner());
        assertEquals(LO_HI, l2.getHint());
        assertNull(l2.getPrivateKey());

        locks.freeLock(lo);

        l2 = locks.getLock(LO_NM);
        assertNull(l2);
    }

    @Test
    public void test_in_1000_thread_write() throws InterruptedException {
        String[] re = new String[1];

        // 搞个信号量
        Object cond = new Object();
        // 先搞一个真的能写的原子
        Atom a0 = new Atom() {
            public void run() {
                WnLock lo = null;
                try {
                    lo = locks.tryLock(LO_NM, LO_OW, LO_HI, 5000000);
                    // 其他的人，可以搞我了
                    Lang.notifyAll(cond);

                    // 我开始写
                    re[0] = "Hello";

                    // 啥也不做，等其他的线程自动退下
                    Thread.sleep(3000);

                    // 释放锁
                    locks.freeLock(lo);
                }
                catch (Exception e) {
                    fail();
                }
            }
        };
        // 再搞一个肯定写失败的原子
        Atom ax = new Atom() {
            public void run() {
                // 先等个3s，直到得到通知
                Lang.wait(cond, 3000);

                // 请求锁，并且不可能得到
                WnLock lo = null;
                try {
                    lo = locks.tryLock(LO_NM, LO_OW, LO_HI, 5000000);
                    fail();
                }
                // 肯定是失败啦
                catch (WnLockFailException e) {

                }
                // 太忙？ 这是不可能的吧
                catch (WnLockBusyException e) {
                    fail(e.toString());
                }
                // 释放 null 锁，不会出错
                finally {
                    try {
                        locks.freeLock(lo);
                    }
                    catch (WnLockBusyException e) {
                        throw Lang.wrapThrow(e);
                    }
                    catch (WnLockNotSameException e) {
                        throw Lang.wrapThrow(e);
                    }
                }
            }
        };

        // 开始搞线程，第一个线程
        Thread t0 = new Thread(a0, "T0");

        // 搞其他的线程
        Thread[] trs = new Thread[1000];
        for (int i = 0; i < trs.length; i++) {
            trs[i] = new Thread(ax, "T-X-" + i);
        }

        // 先启动其他线程
        for (Thread t : trs) {
            t.start();
        }

        // 再启动 0 线程
        t0.start();

        // 等 0 线程结束
        t0.join();

        // 等其他线程结束
        for (Thread t : trs) {
            t.join();
        }

        // 判断
        assertEquals("Hello", re[0]);
    }

}
