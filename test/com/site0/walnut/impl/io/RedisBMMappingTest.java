package com.site0.walnut.impl.io;

import static org.junit.Assert.*;

import org.junit.Test;
import org.nutz.lang.Lang;
import com.site0.walnut.BaseSessionTest;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;

public class RedisBMMappingTest extends BaseSessionTest {

    @Test
    public void test_simple_read_write() {
        // 创建一个路径
        WnObj p = _setup_hierarchy("~/a");

        // 创建一个对象
        WnObj o = io.create(p, "xyz", WnRace.FILE);

        // 判断映射
        assertEquals(p.id(), o.mountRootId());

        // 写入
        io.writeText(o, "hello");
        assertEquals(5, o.len());
        assertEquals(Lang.sha1("hello"), o.sha1());

        // 重新获取
        WnObj o2 = io.get(o.id());
        assertEquals(o.id(), o2.id());
        assertEquals(5, o2.len());
        assertEquals(Lang.sha1("hello"), o2.sha1());

        // 读取
        String str = io.readText(o2);
        assertEquals("hello", str);
    }

    @Test
    public void test_simple_add_get() {
        // 创建一个路径
        WnObj p = _setup_hierarchy("~/a");

        // 创建一个对象
        WnObj o = io.create(p, "xyz", WnRace.FILE);

        // 判断映射
        assertEquals(p.id(), o.mountRootId());

        // 重新获取
        WnObj o2 = io.get(o.id());
        assertEquals(o.id(), o2.id());
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
