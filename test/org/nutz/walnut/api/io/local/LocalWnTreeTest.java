package org.nutz.walnut.api.io.local;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.nutz.lang.Files;
import org.nutz.walnut.api.io.WnNode;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.api.io.WnTree;
import org.nutz.walnut.api.io.WnTreeFactory;
import org.nutz.walnut.impl.WnTreeFactoryImpl;
import org.nutz.walnut.impl.local.LocalWnNode;

public class LocalWnTreeTest {

    private WnTreeFactory factory;

    private WnTree tree;

    @Before
    public void on_before() {
        factory = new WnTreeFactoryImpl();

        // 创建临时目录
        File d = Files.createDirIfNoExists("~/.walnut/junit/tree");

        // 创建树的根节点
        LocalWnNode treeNode = new LocalWnNode(null, d);
        treeNode.genID();
        treeNode.mount("file://" + d.getAbsolutePath());
        tree = factory.check(treeNode);
        tree._clean_for_unit_test();
    }

    @Test
    public void test_create2() {
        tree.create(null, "x/a.txt", WnRace.FILE);
        tree.create(null, "x/b.txt", WnRace.FILE);

        WnNode nd = tree.fetch(null, "x");
        assertEquals(WnRace.DIR, nd.race());
        assertEquals("/x", nd.path());

        nd = tree.fetch(null, "x/a.txt");
        assertEquals(WnRace.FILE, nd.race());
        assertEquals("/x/a.txt", nd.path());

        nd = tree.fetch(null, "x/b.txt");
        assertEquals(WnRace.FILE, nd.race());
        assertEquals("/x/b.txt", nd.path());
    }

    @Test
    public void test_create_delete() {
        tree.create(null, "x/y/z.txt", WnRace.FILE);

        WnNode x = tree.fetch(null, "x");
        assertEquals(WnRace.DIR, x.race());
        assertEquals("/x", x.path());

        WnNode y = tree.fetch(x, "y");
        assertEquals(WnRace.DIR, y.race());
        assertEquals("/x/y", y.path());

        WnNode z = tree.fetch(y, "z.txt");
        assertEquals(WnRace.FILE, z.race());
        assertEquals("/x/y/z.txt", z.path());

        WnNode z2 = tree.fetch(x, "y/z.txt");
        assertEquals(WnRace.FILE, z2.race());

        WnNode z3 = tree.fetch(null, "x/y/z.txt");
        assertEquals(WnRace.FILE, z3.race());

        WnNode z4 = tree.fetch(y, "/x/y/z.txt");
        assertEquals(WnRace.FILE, z4.race());

        assertEquals(z.id(), z2.id());
        assertEquals(z.id(), z3.id());
        assertEquals(z.id(), z4.id());

        tree.delete(y);
        assertNull(tree.fetch(null, "x/y"));
        assertNull(tree.fetch(null, "x/y/z.txt"));
        WnNode x2 = tree.fetch(null, "x");
        assertEquals(x.id(), x2.id());

    }

    @Test
    public void test_mount() {
        WnNode nd = tree.create(null, "workspace/data", WnRace.DIR);
        tree.setMount(nd, "mongo:default");

        assertEquals("mongo:default", nd.mount());
        assertEquals("mongo:default", tree.getNode(nd.id()).mount());

        tree.setMount(nd, null);
        assertNull(tree.getNode(nd.id()).mount());

    }

    @Test
    public void test_mount_another_local_tree() {
        // 创建临时目录
        File d = Files.createDirIfNoExists("~/.walnut/junit/tree2");
        Files.clearDir(d);

        WnNode nd = tree.create(null, "workspace/data", WnRace.DIR);
        tree.setMount(nd, "file://" + d.getAbsolutePath());

        // 创建第二颗树
        WnTree tree2 = tree.factory().check(nd);

        WnNode abc = tree2.create(null, "abc.txt", WnRace.FILE);
        assertTrue(tree2.equals(abc.tree()));
        assertEquals("/workspace/data/abc.txt", abc.path());

        WnNode abc2 = tree2.fetch(null, "abc.txt");
        assertTrue(tree2.equals(abc2.tree()));
        assertEquals(abc.id(), abc2.id());

        // 查找全路径
        WnNode abc3 = tree.fetch(null, "workspace/data/abc.txt");
        assertTrue(tree2.equals(abc3.tree()));
        assertEquals(abc.id(), abc3.id());

        // 从顶级树创建 tree2 的节点
        WnNode readme = tree.create(null, "workspace/data/doc/readme.md", WnRace.FILE);
        WnNode readme2 = tree.fetch(null, "workspace/data/doc/readme.md");

        assertTrue(tree2.equals(readme.tree()));
        assertTrue(tree2.equals(readme2.tree()));
        assertEquals(readme.id(), readme2.id());

        WnNode readme3 = tree2.fetch(null, "doc/readme.md");
        assertTrue(tree2.equals(readme3.tree()));
        assertEquals(readme.id(), readme3.id());

        // 最后清除测试数据
        // Files.deleteDir(d);
    }
}
