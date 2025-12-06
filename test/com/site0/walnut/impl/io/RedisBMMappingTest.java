package com.site0.walnut.impl.io;

import static org.junit.Assert.*;

import org.junit.Test;
import org.nutz.log.Log;

import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.BaseSessionTest;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;

public class RedisBMMappingTest extends BaseSessionTest {
    
    static Log log = Wlog.getTEST();

    @Test
    public void test_simple_read_write() {
        log.info("@Test RedisBMMappingTest.test_simple_read_write Begin");
        // 创建一个路径
        WnObj p = _setup_hierarchy("~/a");

        // 创建一个对象
        WnObj o = io.create(p, "xyz", WnRace.FILE);

        // 判断映射
        assertEquals(p.id(), o.mountRootId());

        // 写入
        io.writeText(o, "hello");
        assertEquals(5, o.len());
        assertEquals(Wlang.sha1("hello"), o.sha1());

        // 重新获取
        WnObj o2 = io.get(o.id());
        assertEquals(o.id(), o2.id());
        assertEquals(5, o2.len());
        assertEquals(Wlang.sha1("hello"), o2.sha1());

        // 读取
        String str = io.readText(o2);
        assertEquals("hello", str);
        log.info("@Test RedisBMMappingTest.test_simple_read_write End");
    }

    @Test
    public void test_simple_add_get() {
        log.info("@Test RedisBMMappingTest.test_simple_add_get Begin");
        // 创建一个路径
        WnObj p = _setup_hierarchy("~/a");

        // 创建一个对象
        WnObj o = io.create(p, "xyz", WnRace.FILE);

        // 判断映射
        assertEquals(p.id(), o.mountRootId());

        // 重新获取
        WnObj o2 = io.get(o.id());
        assertEquals(o.id(), o2.id());
        log.info("@Test RedisBMMappingTest.test_simple_add_get End");
    }

    private WnObj _setup_hierarchy(String dirph) {
        // 映射定义
        String oph = "~/.io/bm/test.json";
        String fph = _CFPH("redis/test.conf.json");
        _write_by(oph, fph);

        // 准备一下映射目录
        WnObj oDir = _created(dirph);

        // 映射索引管理器
        io.setMount(oDir, "://redis(test)");

        // 搞定
        return oDir;
    }

    private static String _CFPH(String nm) {
        return "com/site0/walnut/impl/io/" + nm;
    }

}
