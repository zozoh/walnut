package org.nutz.walnut.api.io;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.walnut.api.io.WnIndexer;
import org.nutz.walnut.api.io.WnObj;

public abstract class AbstractWnIndexerTest {

    @Test
    public void test_simple_get_set() {
        indexer.set("abc", "x", 100);
        indexer.set("abc", "y", 80);

        WnObj o = indexer.get("abc");
        assertEquals(100, o.getInt("x"));
        assertEquals(80, o.getInt("y"));
    }

    // ------------------------------------------------ 这些是测试目标的构建
    protected WnIndexer indexer;

    private PropertiesProxy pp;

    @Before
    public void before() {
        pp = new PropertiesProxy("org/nutz/walnut/junit.properties");
        on_before(pp);
    }

    @After
    public void after() {
        on_after(pp);
    }

    protected abstract void on_before(PropertiesProxy pp);

    protected void on_after(PropertiesProxy pp) {}

}
