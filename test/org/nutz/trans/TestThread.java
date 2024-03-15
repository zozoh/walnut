package org.nutz.trans;

import java.util.ArrayList;
import java.util.List;

import com.site0.walnut.util.Wlang;

import junit.framework.TestCase;

public abstract class TestThread extends Thread {

    public TestThread() {
        results = new ArrayList<Boolean>();
    }

    private List<Boolean> results;

    protected abstract void doTest();

    @Override
    public void run() {
        doTest();
        synchronized (this) {
            try {
                this.wait(1000);
            }
            catch (InterruptedException e) {
                throw Wlang.wrapThrow(e);
            }
        }
    }

    protected void addResult(boolean b) {
        results.add(b);
    }

    public void doAssert() {
        Wlang.each(results, (int index, Boolean b, Object src) -> {
            System.out.println(index);
            TestCase.assertTrue(b);
        });
    }
}
