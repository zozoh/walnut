package org.nutz.walnut.api.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.walnut.WnApiTest;
import org.nutz.walnut.impl.io.WnTreeFactoryImpl;
import org.nutz.walnut.util.Wn;

public abstract class AbstractWnTreeTest extends WnApiTest {

    @Test
    public void test_append() {
        WnNode nd = tree.create(null, "/a/b/c", WnRace.FILE);
        WnNode p = tree.create(null, "/x/y", WnRace.DIR);

        WnNode nd2 = tree.append(p, nd);

        assertEquals(nd.id(), nd2.id());
        assertEquals(p.id(), nd2.parentId());
        assertEquals("/x/y/c", nd2.path());

    }

    public static class FakeWnCallback implements WnSecurity {

        StringBuilder sb_enter = new StringBuilder();
        StringBuilder sb_view = new StringBuilder();
        StringBuilder sb_access = new StringBuilder();
        StringBuilder sb_read = new StringBuilder();
        StringBuilder sb_write = new StringBuilder();

        public <T extends WnNode> T enter(T nd) {
            if (!nd.isRootNode())
                sb_enter.append("/" + nd.name());
            return nd;
        }

        public <T extends WnNode> T view(T nd) {
            sb_view.append("/" + nd.name());
            return nd;
        }

        public <T extends WnNode> T access(T nd) {
            sb_access.append("/" + nd.name());
            return nd;
        }

        public <T extends WnNode> T read(T nd) {
            sb_read.append("/" + nd.name());
            return nd;
        }

        public <T extends WnNode> T write(T nd) {
            sb_write.append("/" + nd.name());
            return nd;
        }
    }

    @Test
    public void test_create_with_security() {
        tree.create(null, "a/b/x", WnRace.FILE);

        FakeWnCallback secu = new FakeWnCallback();
        Wn.WC().setSecurity(secu);
        try {
            tree.create(null, "a/b/c", WnRace.FILE);
        }
        finally {
            Wn.WC().setSecurity(null);
        }

        assertEquals("/a/b", secu.sb_enter.toString());
        assertEquals("", secu.sb_view.toString());
        assertEquals("/a/b", secu.sb_access.toString());
        assertEquals("", secu.sb_read.toString());
        assertEquals("/b", secu.sb_write.toString());
    }

    @Test
    public void test_create_and_check_pid() {
        WnNode nd = tree.create(null, "x/a.txt", WnRace.FILE);
        WnNode pnd = tree.fetch(null, "x");
        assertEquals(pnd.id(), nd.parentId());
        assertEquals(pnd.id(), nd.parent().id());
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
        tree.setMount(nd, "mongo:another");

        assertEquals("mongo:another", nd.mount());
        assertEquals("mongo:another", tree.getNode(nd.id()).mount());

        tree.setMount(nd, null);
        assertNull(tree.getNode(nd.id()).mount());

    }

    @Test
    public void test_mount_another_tree() {
        // 找到一个节点，设置 mount
        WnNode nd = tree.create(null, "workspace/data", WnRace.DIR);
        assertEquals("/workspace/data", nd.path());

        tree.setMount(nd, pp.get(_another_tree_mount_key()));

        // 创建第二颗树
        WnTree tree2 = tree.factory().check(nd);
        tree2.setTreeNode(nd);
        tree2._clean_for_unit_test();

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
        assertEquals("/workspace/data/doc/readme.md", readme2.path());

        WnNode readme3 = tree2.fetch(null, "doc/readme.md");
        assertTrue(tree2.equals(readme3.tree()));
        assertEquals(readme.id(), readme3.id());
        assertEquals("/workspace/data/doc/readme.md", readme3.path());

        // 从顶级树获取 ID
        WnNode readme4 = tree.getNode(readme.id());
        assertTrue(tree2.equals(readme4.tree()));
        assertEquals(readme3.id(), readme4.id());
        readme4.loadParents(null, false);
        assertEquals("/workspace/data/doc/readme.md", readme4.path());

        // 最后清除测试数据
        // Files.deleteDir(d);
    }

    @Test
    public void test_mount_another_tree2() {
        // 找到一个节点，设置 mount
        WnNode nd = tree.create(null, "workspace/data", WnRace.DIR);
        tree.setMount(nd, pp.get(_another_tree_mount_key()));

        // 创建第二颗树
        WnTree tree2 = tree.factory().check(nd);
        tree2.setTreeNode(nd);
        tree2._clean_for_unit_test();

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
        assertEquals("/workspace/data/doc/readme.md", readme2.path());

        WnNode readme3 = tree2.fetch(null, "doc/readme.md");
        assertTrue(tree2.equals(readme3.tree()));
        assertEquals(readme.id(), readme3.id());
        assertEquals("/workspace/data/doc/readme.md", readme3.path());

        // 从顶级树获取 ID
        WnNode readme4 = tree.getNode(readme.id());
        assertTrue(tree2.equals(readme4.tree()));
        assertEquals(readme3.id(), readme4.id());
        readme4.loadParents(null, false);
        assertEquals("/workspace/data/doc/readme.md", readme4.path());

        // 最后清除测试数据
        // Files.deleteDir(d);
    }

    private WnTree tree;

    protected void on_before(PropertiesProxy pp) {
        treeFactory = new WnTreeFactoryImpl(db);
        WnNode nd = _create_top_tree_node();
        tree = treeFactory.check(nd);
        nd.setTree(tree);
        tree._clean_for_unit_test();
    }

    protected abstract String _another_tree_mount_key();

}
