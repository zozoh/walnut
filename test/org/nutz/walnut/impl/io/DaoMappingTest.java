package org.nutz.walnut.impl.io;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.nutz.dao.Dao;
import org.nutz.lang.Lang;
import org.nutz.walnut.BaseSessionTest;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.sql.WnDaoConfig;
import org.nutz.walnut.ext.sql.WnDaos;
import org.nutz.walnut.util.Wn;

public class DaoMappingTest extends BaseSessionTest {

    @Test
    public void test_simple_query() {
        // 准备
        WnObj p = _setup_hierarchy("dao_pet", "~/pets");

        // 创建五个
        io.create(p, "A", WnRace.FILE);
        io.create(p, "B", WnRace.FILE);
        io.create(p, "C", WnRace.FILE);
        io.create(p, "D", WnRace.FILE);
        io.create(p, "E", WnRace.FILE);
        
        WnQuery q = Wn.Q.pid(p);
        io.setBy(q.setv("nm", "A"), Lang.map("age:5"), false);
        io.setBy(q.setv("nm", "B"), Lang.map("age:6"), false);
        io.setBy(q.setv("nm", "C"), Lang.map("age:7"), false);
        io.setBy(q.setv("nm", "D"), Lang.map("age:8"), false);
        io.setBy(q.setv("nm", "E"), Lang.map("age:9"), false);

        // 查一下
        q = Wn.Q.pid(p);
        List<WnObj> list = io.query(q);
        Collections.sort(list);
        assertEquals(5, list.size());
        assertEquals("A", list.get(0).name());
        assertEquals("B", list.get(1).name());
        assertEquals("C", list.get(2).name());
        assertEquals("D", list.get(3).name());
        assertEquals("E", list.get(4).name());

        // 设置一下条件
        q = Wn.Q.pid(p);
        q.setv("age", "[6,8]");
        list = io.query(q);
        Collections.sort(list);
        assertEquals(3, list.size());
        assertEquals("B", list.get(0).name());
        assertEquals("C", list.get(1).name());
        assertEquals("D", list.get(2).name());
    }

    @Test
    public void test_simple_children() {
        // 准备
        WnObj p = _setup_hierarchy("dao_pet", "~/pets");

        // 创建五个
        io.create(p, "A", WnRace.FILE);
        io.create(p, "B", WnRace.FILE);
        io.create(p, "C", WnRace.FILE);
        io.create(p, "D", WnRace.FILE);
        io.create(p, "E", WnRace.FILE);

        // 获取 child
        List<WnObj> list = io.getChildren(p, null);
        Collections.sort(list);
        assertEquals(5, list.size());
        assertEquals("A", list.get(0).name());
        assertEquals("B", list.get(1).name());
        assertEquals("C", list.get(2).name());
        assertEquals("D", list.get(3).name());
        assertEquals("E", list.get(4).name());

        list = io.getChildren(p, "^[ACE]");
        Collections.sort(list);
        assertEquals(3, list.size());
        assertEquals("A", list.get(0).name());
        assertEquals("C", list.get(1).name());
        assertEquals("E", list.get(2).name());
    }

    @Test
    public void test_simple_crud() {
        // 准备
        WnObj p = _setup_hierarchy("dao_pet", "~/pets");

        // 来吧，创建一个文件
        WnObj o = io.create(p, "xiaobai", WnRace.FILE);

        // 那么因为是映射，必然是两段式 ID
        assertEquals(p.id(), o.mountRootId());
        assertTrue(o.isMount());
        assertEquals(p.mount(), o.mount());

        // 获取
        WnObj o1 = io.fetch(p, "xiaobai");

        assertEquals(p.id(), o1.mountRootId());
        assertTrue(o1.isMount());
        assertEquals(p.mount(), o1.mount());

        assertEquals(o.id(), o1.id());
        assertEquals("xiaobai", o1.name());
        assertTrue(o1.isFILE());

        // 测试采用 ID 获取
        WnObj o2 = io.get(o.id());

        assertEquals(p.id(), o2.mountRootId());
        assertTrue(o2.isMount());
        assertEquals(p.mount(), o2.mount());

        assertEquals(o.id(), o2.id());
        assertEquals("xiaobai", o2.name());
        assertTrue(o2.isFILE());

        // 设置元数据
        WnObj o3 = io.setBy(o2.id(), Lang.map("age", 12), true);
        assertEquals(12, o3.getInt("age"));

        WnObj o4 = io.get(o3.id());
        assertEquals(12, o4.getInt("age"));

        // 修改元数据
        assertEquals(17, io.inc(o.id(), "age", 5, true));

        WnObj o5 = io.get(o3.id());
        assertEquals(17, o5.getInt("age"));

        // 删除
        io.delete(o5);
        assertFalse(io.existsId(o3.id()));
        assertFalse(io.exists(p, "xiaobai"));

    }

    private WnObj _setup_hierarchy(String cfnm, String dirph) {
        // 数据源的定义
        _write_by("~/.dao/demo.dao.json", _CFPH("dao/demo.dao.json"));

        // 映射定义
        String oph = "~/.io/ix/" + cfnm + ".json";
        String fph = _CFPH("ix/" + cfnm + ".json");
        _write_by(oph, fph);

        // 准备一下映射目录
        WnObj oDir = _created(dirph);

        // 映射索引管理器
        io.setMount(oDir, "dao(" + cfnm + ")");

        // 读取 Dao 配置
        WnDaoConfig conf = WnDaos.loadConfig(io, oph, this.session);
        Dao dao = WnDaos.get(conf);

        // 清理数据
        dao.drop(conf.getTableName());

        // 搞定
        return oDir;
    }

    private static String _CFPH(String nm) {
        return "org/nutz/walnut/impl/io/" + nm;
    }

}