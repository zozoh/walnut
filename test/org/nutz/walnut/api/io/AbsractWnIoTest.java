package org.nutz.walnut.api.io;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.nutz.json.Json;
import org.nutz.lang.Each;
import org.nutz.lang.Encoding;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.lang.util.Callback;
import org.nutz.lang.util.Disks;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.cheap.dom.CheapDocument;
import org.nutz.walnut.cheap.xml.CheapXmlParsing;
import org.nutz.walnut.core.IoCoreTest;
import org.nutz.walnut.core.WnIoHandle;
import org.nutz.walnut.core.WnIoHandleManager;
import org.nutz.walnut.core.WnIoHandleMutexException;
import org.nutz.walnut.core.WnReferApi;
import org.nutz.walnut.core.bean.WnIoObj;
import org.nutz.walnut.core.bean.WnObjIdTest;
import org.nutz.walnut.core.bm.localbm.LocalIoBM;
import org.nutz.walnut.core.bm.redis.RedisBM;
import org.nutz.walnut.core.indexer.localfile.WnLocalFileObj;
import org.nutz.walnut.impl.io.WnEvalLink;
import org.nutz.walnut.util.Wn;
import org.nutz.web.WebException;

public abstract class AbsractWnIoTest extends IoCoreTest {

    /**
     * 子类需要设置这三个实例
     */
    protected WnIo io;
    protected WnReferApi refers;
    protected WnIoHandleManager handles;

    @Test
    public void test_append_meta_remove_key() {
        WnObj o = io.create(null, "/a/b/c", WnRace.FILE);
        NutMap meta = Lang.map("hello", "world");
        io.appendMeta(o, meta);
        assertEquals("world", o.getString("hello"));

        meta = Lang.map("!hello", true);
        io.appendMeta(o, meta);
        assertFalse(o.has("hello"));
    }

    /**
     * 获取一个本地文件映射对象，并能顺利的取到 pid
     * 
     * <pre>
     * /mnt/dir  ::=>  local_home::/a
     * |-- b/      
     *     |-- c/   
     *     |   |-- d/
     *     |       |-- x.txt
     * 
     * 
     * </pre>
     */
    @Test
    public void test_get_pid_of_file_mount_file() {
        // 准备一个本地目录
        File dHome = setup.getLocalFileHome();
        File f0 = Files.getFile(dHome, "a/b/c/d/x.txt");
        Files.createFileIfNoExists(f0);

        File dMntHome = Files.getFile(dHome, "a");
        String phMntHome = Disks.getCanonicalPath(dMntHome.getAbsolutePath());

        // 建立映射目录
        WnObj oMntDir = io.create(null, "/mnt/dir", WnRace.DIR);
        io.setMount(oMntDir, "filew://" + phMntHome);

        // 获取文件 /home/demo/dir/a/b/c/d/x.txt
        WnObj o = io.fetch(null, "/mnt/dir/b/c/d/x.txt");
        assertEquals(oMntDir.id() + ":b/c/d/x.txt", o.id());
        assertEquals(oMntDir.id() + ":b/c/d/", o.parentId());
        assertEquals(o.parentId(), o.getString("pid"));
    }

    /**
     * 删除一个 filew 映射的文件
     * 
     * <pre>
     * /mnt/dir  ::=>  local_home::/a
     * |-- b/      
     *     |-- c/   
     *     |   |-- d/   <-- LINK oBase: /home/demo/dir
     *     |       |-- x.txt
     *     |       |-- y.txt
     *     |       |-- z.txt
     * 然后准备基线
     *  oBase : fetch /mnt/dir/a/b/c
     *  
     * 根据基线的查询，应该能获取三个文件
     * 
     * 
     * </pre>
     */
    @Test
    public void test_query_by_pid_in_file_mount_file() {
        // 准备一个本地目录
        File dHome = setup.getLocalFileHome();
        File f0 = Files.getFile(dHome, "a/b/c/d/x.txt");
        Files.createFileIfNoExists(f0);
        File f1 = Files.getFile(dHome, "a/b/c/d/y.txt");
        Files.createFileIfNoExists(f1);
        File f2 = Files.getFile(dHome, "a/b/c/d/z.txt");
        Files.createFileIfNoExists(f2);

        File dMntHome = Files.getFile(dHome, "a");
        String phMntHome = Disks.getCanonicalPath(dMntHome.getAbsolutePath());

        // 建立映射目录
        WnObj oMntDir = io.create(null, "/mnt/dir", WnRace.DIR);
        io.setMount(oMntDir, "filew://" + phMntHome);

        // 准备一个基线文件
        WnObj oBase = io.create(null, "/home/demo/dir", WnRace.DIR);
        io.appendMeta(oBase, Lang.map("ln", "/mnt/dir/b/c/d/"));

        // 根据基线的查询，应该能获取三个文件
        WnQuery q = Wn.Q.pid(oBase).sort(Lang.map("nm:1"));
        List<WnObj> list = io.query(q);
        assertEquals(3, list.size());
        assertEquals("x.txt", list.get(0).name());
        assertEquals("y.txt", list.get(1).name());
        assertEquals("z.txt", list.get(2).name());

        // 重新查一遍，确保没问题
        q = Wn.Q.pid(oBase).sort(Lang.map("nm:1"));
        oBase = io.fetch(null, "/home/demo/dir");
        list = io.query(q);
        assertEquals(3, list.size());
        assertEquals("x.txt", list.get(0).name());
        assertEquals("y.txt", list.get(1).name());
        assertEquals("z.txt", list.get(2).name());

        // 那么查询的时候，排序一下呢
        q = Wn.Q.pid(oBase).sort(Lang.map("nm:-1"));
        oBase = io.fetch(null, "/home/demo/dir");
        list = io.query(q);
        assertEquals(3, list.size());
        assertEquals("z.txt", list.get(0).name());
        assertEquals("y.txt", list.get(1).name());
        assertEquals("x.txt", list.get(2).name());
    }

    /**
     * 删除一个 filew 映射的文件
     * 
     * <pre>
     * /mnt/dir  ::=>  local_home::/a
     * |-- b/      
     *     |-- c/   
     *     |   |-- d/   <-- oBase
     *     |       |-- xyz.txt
     *     |-- x/
     *         |-- y/  <-- oDir by ../../x/y/
     *             |-- abc.txt       
     * 然后准备基线
     *  oBase : fetch /mnt/dir/a/b/c
     *  
     * 根据相对路径获取
     *  oDir : fetch (oBase, "../../x/y/")
     * 
     * 应该得到的是 /a/b/ 这个目录
     * 
     * 获取 children，应该有个 c
     * 
     * </pre>
     */
    @Test
    public void test_get_parent_path_in_file_mount_file() {
        // 准备一个本地目录
        File dHome = setup.getLocalFileHome();
        File f0 = Files.getFile(dHome, "a/b/c/d/xyz.txt");
        Files.createFileIfNoExists(f0);
        File f1 = Files.getFile(dHome, "a/b/x/y/abc.txt");
        Files.createFileIfNoExists(f1);

        File dMntHome = Files.getFile(dHome, "a");
        String phMntHome = Disks.getCanonicalPath(dMntHome.getAbsolutePath());

        // 建立映射目录
        WnObj oMntDir = io.create(null, "/mnt/dir", WnRace.DIR);
        io.setMount(oMntDir, "filew://" + phMntHome);

        // 获取基线
        WnObj oBase = io.fetch(null, "/mnt/dir/b/c/d");
        assertEquals("d", oBase.name());
        assertTrue(oBase.isDIR());

        // 根据相对路径获取
        WnObj oDir = io.fetch(oBase, "../../x/y/");
        assertEquals("y", oDir.name());
        assertTrue(oDir.isDIR());
        assertEquals("/mnt/dir/b/x/y", oDir.path());
    }

    /**
     * 删除一个 filew 映射的文件
     * 
     * <pre>
     * /mnt/dir  ::=>  local_home::/a
     * |-- b/
     *     |-- c/
     *         |-- xyz.txt
     * 然后
     * /home/demo/dir ->  /mnt/dir/b/c
     * 
     * 那么自然通过 "/home/demo/dir/xyz.txt" 可以得到文件，并可读写
     * 可以 each 下面的内容
     * 删除的话，也能删除
     * 也可以创建一个新的（即使重新获取父的话）
     * </pre>
     */
    @Test
    public void test_create_and_remove_filew_mount_file() {
        // 准备一个本地目录
        File dHome = setup.getLocalFileHome();
        File f = Files.getFile(dHome, "a/b/c/xyz.txt");
        Files.createFileIfNoExists(f);
        File dMntHome = Files.getFile(dHome, "a");
        String phMntHome = Disks.getCanonicalPath(dMntHome.getAbsolutePath());

        // 建立映射目录
        WnObj oMntDir = io.create(null, "/mnt/dir", WnRace.DIR);
        io.setMount(oMntDir, "filew://" + phMntHome);

        // 建立链接目录
        WnObj oLnkDir = io.create(null, "/home/demo/dir", WnRace.DIR);
        io.appendMeta(oLnkDir, Lang.map("ln", "/mnt/dir/b/c/"));

        // 那么自然通过 "/home/demo/dir/xyz.txt" 可以得到文件，并可读写
        WnObj o = io.fetch(null, "/home/demo/dir/xyz.txt");
        assertEquals("xyz.txt", o.name());

        // 写
        io.writeText(o, "hello");
        String text = Files.read(f);
        assertEquals("hello", text);

        Files.write(f, "world");
        text = io.readText(o);
        assertEquals("world", text);

        // 可以 each
        WnEvalLink wnEvalLink = new WnEvalLink(io);
        WnObj oP = io.fetch(null, "/home/demo/dir");
        oP = wnEvalLink.enter(oP, false);

        WnQuery q = Wn.Q.pid(oP);
        List<WnObj> list = io.query(q);
        assertEquals(1, list.size());
        assertEquals("xyz.txt", list.get(0).name());
        assertEquals(o.id(), list.get(0).id());

        // 也可以删除
        assertTrue(f.exists());
        io.delete(o);
        assertFalse(f.exists());

        // 也可以创建一个新的（即使重新获取父的话）
        oP = io.fetch(null, "/home/demo/dir");
        WnObj o2 = io.create(oP, "test", WnRace.DIR);
        assertTrue((o2 instanceof WnLocalFileObj));
        assertTrue(o2.hasMountRootId());
        assertFalse(o2.isLink());

        File dir = Files.getFile(dHome, "a/b/c/test");
        assertTrue(dir.exists());
        assertTrue(dir.isDirectory());

        // 也可以删除
        o2 = io.fetch(null, "/home/demo/dir/test");
        assertTrue(o2.isDIR());
        io.delete(o2);
        assertFalse(dir.exists());

        // 那么采用对象的方式创建
        o2 = new WnIoObj().name("test").race(WnRace.DIR);
        io.create(oP, o2);

        assertTrue(dir.exists());
        assertTrue(dir.isDirectory());

        // 也可以删除
        o2 = io.fetch(null, "/home/demo/dir/test");
        assertTrue(o2.isDIR());
        io.delete(o2);
        assertFalse(dir.exists());
    }

    @Test
    public void test_create_by_id_parent_dir() {
        WnObj dir = io.create(null, "/a/b/c", WnRace.DIR);
        WnObj obj = io.create(null, "id:" + dir.id() + "/abc.txt", WnRace.FILE);

        WnObj o2 = io.fetch(null, "/a/b/c/abc.txt");
        assertEquals(obj.id(), o2.id());
        assertEquals("abc.txt", o2.name());
    }

    @Test
    public void test_get_silbing_in_file_mount() {
        WnObj dTi = io.create(null, "/rs/ti", WnRace.DIR);
        io.appendMeta(dTi, Lang.map("ln:'/mnt/ti'"));

        File dHome = setup.getLocalFileHome();
        File f = Files.getFile(dHome, "titanium/src/view/creation.json");
        Files.createFileIfNoExists(f);
        File f2 = Files.getFile(dHome, "titanium/src/view/types/zh-cn/_types.json");
        Files.write(f2, "hello");
        Files.createFileIfNoExists(f2);

        WnObj dMnTi = io.create(null, "/mnt/ti", WnRace.DIR);
        String aph = Files.getAbsPath(dHome);
        String mnt = "file://" + aph + "/titanium/src";
        io.setMount(dMnTi, mnt);

        WnObj oC = io.fetch(null, "/rs/ti/view/creation.json");

        // 测试一下获取父
        WnObj oView = oC.parent();
        assertEquals("/rs/ti/view", oView.path());
        assertEquals(WnRace.DIR, oView.race());

        WnObj oTi = oView.parent();
        assertEquals("/rs/ti", oTi.path());
        assertEquals(dMnTi.id(), oTi.id());

        WnObj oT = io.fetch(oC, "types/zh-cn/_types.json");

        assertEquals("_types.json", oT.name());
        String str = io.readText(oT);
        assertEquals("hello", str);
    }

    @Test
    public void test_get_silbing_file() {
        io.create(null, "/a/x", WnRace.FILE);
        io.create(null, "/a/y", WnRace.FILE);

        WnObj oX = io.fetch(null, "/a/x");
        WnObj oY = io.fetch(oX, "y");

        assertEquals("y", oY.name());
    }

    @Test
    public void test_mnt_global_and_redisBM() {
        WnObj p = io.create(null, "/var/session", WnRace.DIR);
        io.setMount(p, "://redis(_)");
        String str;

        // 创建一个文件
        WnObj o = io.create(p, "a/b/c.txt", WnRace.FILE);
        assertNull(o.get("mnt"));
        assertEquals(p.mount(), o.mount());
        assertTrue(o.isMount());

        // 写一个
        io.writeText(o, "hello");

        // 在 Redis 里是存在的
        RedisBM bm = setup.getRedisBM();
        byte[] bs = bm.getBytes(o.myId());
        str = new String(bs);
        assertEquals("hello", str);

        str = io.readText(o);
        assertEquals("hello", str);
    }

    @Test
    public void test_walk_in_mount_2() {
        WnObj dTi = io.create(null, "/rs/ti", WnRace.DIR);
        io.appendMeta(dTi, Lang.map("ln:'/mnt/ti/src'"));

        File dHome = setup.getLocalFileHome();
        File f = Files.getFile(dHome, "titanium/src/view/creation.json");
        Files.createFileIfNoExists(f);
        WnObj dMnTi = io.create(null, "/mnt/ti", WnRace.DIR);
        String aph = Files.getAbsPath(dHome);
        String mnt = "file://" + aph + "/titanium/";
        io.setMount(dMnTi, mnt);

        WnObj o = io.fetch(null, "/rs/ti/");
        List<WnObj> list = new ArrayList<>(3);
        io.walk(o, new Callback<WnObj>() {
            public void invoke(WnObj obj) {
                list.add(obj);
            }
        }, WalkMode.DEPTH_NODE_FIRST);

        assertEquals(2, list.size());
        assertEquals("view", list.get(0).name());
        assertEquals("creation.json", list.get(1).name());
    }

    @Test
    public void test_link_in_mount_2() {
        WnObj dTi = io.create(null, "/rs/ti", WnRace.DIR);
        io.appendMeta(dTi, Lang.map("ln:'/mnt/ti/src'"));

        File dHome = setup.getLocalFileHome();
        File f = Files.getFile(dHome, "titanium/src/view/creation.json");
        Files.createFileIfNoExists(f);
        WnObj dMnTi = io.create(null, "/mnt/ti", WnRace.DIR);
        String aph = Files.getAbsPath(dHome);
        String mnt = "file://" + aph + "/titanium/";
        io.setMount(dMnTi, mnt);

        WnObj o = io.fetch(null, "/rs/ti/view/creation.json");
        assertEquals("/rs/ti/view/creation.json", o.path());

        WnObj p = o.parent();
        assertEquals("/rs/ti/view", p.path());
        assertTrue(o.isMount());
        assertFalse(p.isLink());
        assertEquals(mnt, o.mount());

        p = p.parent();
        assertEquals("/rs/ti", p.path());
        assertTrue(p.isMount());
        assertFalse(p.isLink());
        assertEquals(mnt, p.mount());

        p = p.parent();
        assertEquals("/rs", p.path());
        assertFalse(p.isMount());
        assertFalse(p.isLink());

        p = p.parent();
        assertEquals("/", p.path());
        assertFalse(p.isMount());
        assertFalse(p.isLink());
    }

    @Test
    public void test_append_complex_meta() {
        WnObj o = io.create(null, "/a/b/c", WnRace.FILE);
        File f = Files.findFile("org/nutz/walnut/core/req_meta.json");
        NutMap meta = Json.fromJsonFile(NutMap.class, f);

        io.appendMeta(o, meta);
        WnObj o2 = io.get(o.id());
        assertEquals("demo", o2.getString("http-usr"));
        assertEquals("http://api.local.io:8080/api/demo/thing/list", o2.getString("http-url"));
        for (String key : meta.keySet()) {
            Object val = meta.get(key);
            Object v2 = o2.get(key);
            assertTrue(val.equals(v2));
        }
    }

    @Test
    public void test_write_cause_2_refer() {
        WnObj f1 = io.create(null, "/f1", WnRace.FILE);
        WnObj f2 = io.create(null, "/f2", WnRace.FILE);

        io.writeText(f1, "hello");
        String sha1 = f1.sha1();

        assertEquals(1, refers.count(sha1));

        io.writeText(f2, "hello");
        assertEquals(2, refers.count(sha1));

        Set<String> ids = refers.all(sha1);
        assertEquals(2, ids.size());
        HashMap<String, Boolean> map = new HashMap<>();
        for (String id : ids) {
            map.put(id, true);
        }

        assertTrue(map.get(f1.id()));
        assertTrue(map.get(f2.id()));
    }

    @Test
    public void test_fnm_special_0() {
        WnObj a = io.create(null, "/a/b/c", WnRace.DIR);
        WnObj fx = io.create(a, "x(1).txt", WnRace.FILE);

        WnObj b = io.check(null, "/a/b");
        WnObj fx2 = io.fetch(b, "c/x(1).txt");
        assertEquals(fx.id(), fx2.id());
    }

    @Test
    public void test_pid_to_self() {
        WnObj a = io.create(null, "a", WnRace.DIR);
        WnObj b = io.create(a, "b", WnRace.DIR);

        io.move(b, "/a/b");

        WnObj b2 = io.get(b.id());
        assertEquals("b", b2.name());
        assertEquals(a.id(), b2.parentId());

        b.setv("pid", b.id());
        try {
            io.set(b, "^pid$");
            fail();
        }
        catch (WebException e) {
            assertEquals("e.io.mv.parentToChild", e.getKey());
        }

        WnObj b3 = io.get(b.id());
        assertEquals("b", b3.name());
        assertEquals(a.id(), b3.parentId());

    }

    @Test
    public void test_setBy_query() {
        WnObj a = io.create(null, "/a.txt", WnRace.FILE);
        WnObj b = io.create(null, "/b.txt", WnRace.FILE);
        WnObj c = io.create(null, "/c.txt", WnRace.FILE);

        // 设置条件
        io.appendMeta(a, Lang.map("age:10, weight:10"));
        io.appendMeta(b, Lang.map("age:12, weight:14"));
        io.appendMeta(c, Lang.map("age:12, weight:10"));

        // 修改的字段
        // 修改一个，没条件应该是 null
        WnObj o = io.setBy(Wn.Q.map("{}"), Lang.map("realname:'xiaobai', nb:24"), false);
        assertNull(o);

        // 修改 "{age:12}" 的，只有一个修改了
        o = io.setBy(Wn.Q.map("{age:12}"), Lang.map("realname:'xiaobai', nb:24"), false);
        assertTrue(b.isSameId(o) || c.isSameId(o));
        assertEquals("xiaobai", io.getString(o.id(), "realname", null));
        assertEquals(24, io.getInt(o.id(), "nb", 0));

        // 修改 "{age:12, weight:11}" 的返回 null
        o = io.setBy(Wn.Q.map("{age:12, weight:11}"), Lang.map("brief:'AAA', x:3001"), false);
        assertNull(o);

        // 修改 "{age:12 weight:14}" 的返回只有一个被修改了
        o = io.setBy(Wn.Q.map("{age:12, weight:14}"), Lang.map("brief:'AAA', x:3001"), true);
        assertTrue(b.isSameId(o));
        assertEquals("AAA", io.getString(o.id(), "brief", null));
        assertEquals(3001, io.getInt(o.id(), "x", 0));
        assertEquals("AAA", o.getString("brief"));
        assertEquals(3001, o.getInt("x"));
    }

    @Test
    public void test_setBy() {
        WnObj a = io.create(null, "/a.txt", WnRace.FILE);

        WnObj a1 = io.setBy(a.id(), Lang.map("x:100,y:80"), false);
        assertEquals(a.id(), a1.id());
        assertEquals(-1, a1.getInt("x"));
        assertEquals(-1, a1.getInt("y"));

        WnObj a2 = io.get(a.id());
        assertEquals(a.id(), a2.id());
        assertEquals(100, a2.getInt("x"));
        assertEquals(80, a2.getInt("y"));
    }

    @Test
    public void test_copyData() {
        WnObj a = io.create(null, "/a.txt", WnRace.FILE);
        WnObj b = io.create(null, "/b.txt", WnRace.FILE);

        io.writeText(a, "haha");
        io.copyData(a, b);
        assertEquals(4, b.len());
        assertEquals(4, io.get(b.id()).len());
        assertTrue(a.isSameSha1(b.sha1()));
        assertTrue(io.get(a.id()).isSameSha1(io.get(b.id()).sha1()));

        io.writeText(a, "xyzmmm");
        io.copyData(a, b);
        assertEquals(6, b.len());
        assertEquals(6, io.get(b.id()).len());
        assertTrue(a.isSameSha1(b.sha1()));
        assertTrue(io.get(a.id()).isSameSha1(io.get(b.id()).sha1()));
    }

    @Test
    public void test_write_empty_file_by_ins() {
        WnObj o = io.create(null, "/a.txt", WnRace.FILE);

        io.writeText(o, "haha");
        assertEquals("haha", io.readText(o));
        assertEquals(4, o.len());
        assertEquals(4, io.get(o.id()).len());

        OutputStream ops = io.getOutputStream(o, 0);
        Streams.writeAndClose(ops, Lang.ins(""));
        assertEquals("", io.readText(o));
        assertEquals(0, o.len());
        assertEquals(0, io.get(o.id()).len());
    }

    @Test
    public void test_write_empty_file_by_text() {
        WnObj o = io.create(null, "/a.txt", WnRace.FILE);

        io.writeText(o, "haha");
        assertEquals("haha", io.readText(o));
        assertEquals(4, o.len());
        assertEquals(4, io.get(o.id()).len());

        io.writeText(o, "");
        assertEquals("", io.readText(o));
        assertEquals(0, o.len());
        assertEquals(0, io.get(o.id()).len());
    }

    @Test
    public void test_fetch_link_mnt_obj() {
        // 创建临时文件夹，并写入两个文件
        File d = setup.getLocalFileHome();
        String aph = Files.getAbsPath(d);
        Files.write(d.getAbsolutePath() + "/aa", "AAA");
        Files.write(d.getAbsolutePath() + "/bb", "BBB");

        // 创建一个映射文件一个链接文件
        WnObj oM = io.create(null, "/test/m", WnRace.DIR);
        WnObj o = io.create(null, "/mydir/a", WnRace.DIR);

        io.setMount(oM, "file://" + aph);

        o.link("/test/m");
        io.set(o, "^ln$");

        // 试着读取一下
        WnObj o_a = io.check(null, "/mydir/a/aa");
        assertEquals("/mydir/a/aa", o_a.path());
        assertEquals("AAA", io.readText(o_a));

        WnObj o_b = io.check(null, "/mydir/a/bb");
        assertEquals("/mydir/a/bb", o_b.path());
        assertEquals("BBB", io.readText(o_b));

    }

    @Test
    public void test_create_delete_ln_obj() {
        WnObj c = io.create(null, "/a/b/c", WnRace.DIR);
        WnObj o = io.create(null, "/m", WnRace.FILE);
        o.link("/a");
        io.set(o, "^ln$");

        o = io.fetch(null, "/m");

        assertTrue(o.isLink());

        WnObj c2 = io.fetch(o, "b/c");
        assertEquals(c.id(), c2.id());

        io.delete(o);
        assertNull(io.fetch(null, "/m"));
    }

    @Test
    public void test_write_json_obj() {
        WnObj o = io.create(null, "/a", WnRace.FILE);
        NutMap map = Lang.map("x:100,y:10");
        io.writeJson(o, map, null);

        NutMap map2 = io.readJson(o, NutMap.class);
        assertTrue(Lang.equals(map, map2));
    }

    @Test
    public void test_fetch_by_special_path() {
        WnObj o = io.create(null, "/a/b/c/d", WnRace.FILE);

        WnObj f = io.fetch(null, "/a/b/c/d/.");
        assertEquals(o.id(), f.id());

        f = io.fetch(null, "/a/b/./c/./d");
        assertEquals(o.id(), f.id());

        f = io.fetch(null, "/a/b/../b/c/d");
        assertEquals(o.id(), f.id());
    }

    @Test
    public void test_quey_not() {
        WnObj a = io.create(null, "/HT/a", WnRace.FILE);
        io.appendMeta(a, "x:100");
        WnObj b = io.create(null, "/HT/b", WnRace.FILE);
        io.appendMeta(b, "x:99");
        WnObj c = io.create(null, "/HT/c", WnRace.FILE);
        io.appendMeta(c, "x:98");

        WnObj oHT = io.check(null, "/HT");

        WnQuery q = Wn.Q.pid(oHT);
        q.setv("x", Lang.map("$ne:100")).asc("nm");
        List<WnObj> list = io.query(q);
        assertEquals(2, list.size());
        assertEquals(b.id(), list.get(0).id());
        assertEquals(c.id(), list.get(1).id());
    }

    @Test
    public void test_query_no_null() {
        WnObj a = io.create(null, "/HT/a", WnRace.FILE);
        io.appendMeta(a, "alias:'aaa'");
        WnObj b = io.create(null, "/HT/b", WnRace.FILE);
        io.appendMeta(b, "alias:null");
        WnObj c = io.create(null, "/HT/c", WnRace.FILE);

        WnObj oHT = io.check(null, "/HT");

        WnQuery q = Wn.Q.pid(oHT);
        q.setv("alias", Lang.map("$ne:null"));
        List<WnObj> list = io.query(q);
        assertEquals(1, list.size());
        assertEquals(a.id(), list.get(0).id());

        q = Wn.Q.pid(oHT);
        q.setv("alias", null).asc("nm");
        list = io.query(q);
        assertEquals(2, list.size());
        assertEquals(b.id(), list.get(0).id());
        assertEquals(c.id(), list.get(1).id());

        q = Wn.Q.pid(oHT);
        q.setv("$and", Json.fromJson("[{alias:null}, {alias:{$exists:true}}]"));
        list = io.query(q);
        assertEquals(1, list.size());
        assertEquals(b.id(), list.get(0).id());

        q = Wn.Q.pid(oHT);
        q.setv("alias", Lang.map("$exists:true")).asc("nm");
        list = io.query(q);
        assertEquals(2, list.size());
        assertEquals(a.id(), list.get(0).id());
        assertEquals(b.id(), list.get(1).id());

        q = Wn.Q.pid(oHT);
        q.setv("alias", Lang.map("$exists:false"));
        list = io.query(q);
        assertEquals(1, list.size());
        assertEquals(c.id(), list.get(0).id());

    }

    @Test
    public void test_get_meta_directly() {
        WnObj o = io.create(null, "/abc", WnRace.FILE);
        o.setv("x", 100);
        o.setv("y", 4000);
        o.setv("map", "{txt:'haha'}");
        io.set(o, "^(x|y|map)$");

        String id = o.id();

        assertEquals(100, io.getInt(id, "x", -1));
        assertEquals(4000, io.getLong(id, "y", -1));
        assertEquals("abc", io.getString(id, "nm", null));
        NutMap map = io.getAs(id, "map", NutMap.class, null);
        assertEquals(1, map.size());
        assertEquals("haha", map.get("txt"));
    }

    @Test
    public void test_write_empty_text() {
        WnObj o = io.create(null, "/abc", WnRace.FILE);
        io.writeText(o, "AA");
        assertEquals(2, o.len());
        assertEquals(2, io.check(null, "/abc").len());

        io.writeText(o, "");
        assertEquals(0, o.len());
        assertEquals(0, io.check(null, "/abc").len());
    }

    @Test
    public void test_read_bytes() throws UnsupportedEncodingException {
        WnObj o = io.create(null, "/a.xml", WnRace.FILE);
        String path = "org/nutz/walnut/core/test.xml";
        String xml = Files.read(path);
        io.writeText(o, xml);

        byte[] b1 = Files.readBytes(path);
        byte[] b2 = io.readBytes(o);

        assertEquals(b1.length, b2.length);
        String s1 = new String(b1, Encoding.UTF8);
        String s2 = new String(b2, Encoding.UTF8);

        assertEquals(s1, s2);
    }

    @Test
    public void test_read_text() {
        WnObj o = io.create(null, "/a.xml", WnRace.FILE);
        String path = "org/nutz/walnut/core/test.xml";
        String xml = Files.read(path);
        io.writeText(o, xml);

        String exp = Files.read(path);

        InputStream ins = io.getInputStream(o, 0);
        String str = Streams.readAndClose(Streams.utf8r(ins));

        assertEquals(exp, str);
    }

    @Test
    public void test_read_xml() {
        WnObj o = io.create(null, "/a.xml", WnRace.FILE);
        String path = "org/nutz/walnut/core/test.xml";
        String xml = Files.read(path);
        io.writeText(o, xml);

        String input = io.readText(o);
        CheapXmlParsing ing = new CheapXmlParsing("a");
        CheapDocument doc = ing.parseDoc(input);
        assertEquals("a", doc.root().getTagName());
    }

    @Test
    public void test_inc() {
        WnObj o = io.create(null, "/a/b/c", WnRace.FILE);
        o.setv("nb", 10);
        io.set(o, "nb");

        assertEquals(10, io.inc(o.id(), "nb", -1, false));
        assertEquals(9, io.inc(o.id(), "nb", 3, false));
        assertEquals(12, io.inc(o.id(), "nb", -2, false));

        assertEquals(9, io.inc(o.id(), "nb", -1, true));
        assertEquals(109, io.inc(o.id(), "nb", 100, true));
        assertEquals(-1, io.inc(o.id(), "nb", -110, true));

    }

    @Test
    public void test_write_empty() {
        String path = "/a/b/c";
        WnObj o = io.create(null, path, WnRace.FILE);
        io.writeText(o, "");
        WnObj o2 = io.check(null, "/a/b/c");
        assertNull(o2.get("_write_handle"));
    }

    /**
     * for issue #29
     */
    @Test
    public void test_query_and_read() {
        WnObj o = io.create(null, "/a/b/c", WnRace.FILE);
        io.writeText(o, "hello");

        WnQuery q = new WnQuery();
        q.setv("nm", "c");

        o = io.query(q).get(0);
        String txt = io.readText(o);

        assertEquals("hello", txt);
    }

    @Test
    public void test_short_id() {
        WnObj p = io.create(null, "/tt", WnRace.DIR);
        WnObj a = io.createById(p, "a003vgv123c", "a", WnRace.DIR);
        WnObj b = io.createById(p, "a029hd83219", "b", WnRace.DIR);

        try {
            io.get("a0");
            fail();
        }
        catch (WebException e) {
            assertEquals("e.io.obj.get.shortid : a0", e.toString());
        }

        WnObj a2 = io.checkById("a00");
        assertEquals(a.id(), a2.id());
        assertEquals(a.path(), a2.path());
        WnObj b2 = io.checkById("a02");
        assertEquals(b.id(), b2.id());
        assertEquals(b.path(), b2.path());
    }

    @Test
    public void test_update_dn() {
        WnObj o = io.create(null, "/a/b/c", WnRace.FILE);
        assertEquals("a", o.d0());
        assertEquals("b", o.d1());

        io.move(o, "/a");
        assertEquals("a", o.d0());
        assertEquals("c", o.d1());

        o.d0("H").d1("Y");
        io.set(o, "^(d0|d1)$");

        WnObj o2 = io.get(o.id());
        assertEquals("a", o2.d0());
        assertEquals("c", o2.d1());
    }

    @Test
    public void test_create_by_id() {
        WnObj a = io.createById(null, "id0", "a", WnRace.DIR);
        WnObj b = io.createById(a, "id1", "b", WnRace.DIR);
        WnObj c = io.createById(b, "id2", "c", WnRace.FILE);

        assertEquals("id0", a.id());
        assertEquals("a", a.name());
        assertEquals("id1", b.id());
        assertEquals("b", b.name());
        assertEquals("id2", c.id());
        assertEquals("c", c.name());

        a = io.check(null, "/a");
        b = io.check(null, "/a/b");
        c = io.check(null, "/a/b/c");

        assertEquals("id0", a.id());
        assertEquals("a", a.name());
        assertEquals("id1", b.id());
        assertEquals("b", b.name());
        assertEquals("id2", c.id());
        assertEquals("c", c.name());

        a = io.checkById("id0");
        b = io.checkById("id1");
        c = io.checkById("id2");

        assertEquals("id0", a.id());
        assertEquals("a", a.name());
        assertEquals("id1", b.id());
        assertEquals("b", b.name());
        assertEquals("id2", c.id());
        assertEquals("c", c.name());
    }

    @Test
    public void test_move_chtype() {
        io.create(null, "/m/n", WnRace.DIR);
        WnObj c = io.create(null, "/a/b/c", WnRace.FILE);
        WnObj o = io.move(c, "/m/n/xyz.js");

        assertEquals("xyz.js", o.name());
        assertEquals("/m/n/xyz.js", o.path());
        assertEquals("js", o.type());
        assertEquals(mimes().getMime("js"), o.mime());
    }

    @Test
    public void test_move_2() {
        io.create(null, "/abc", WnRace.DIR);

        WnObj x = io.create(null, "/x", WnRace.DIR);
        WnObj y = io.create(null, "/y", WnRace.DIR);
        WnObj z = io.create(null, "/z", WnRace.DIR);

        assertEquals("x", x.d0());
        assertEquals("y", y.d0());
        assertEquals("z", z.d0());
        assertNull(x.d1());
        assertNull(y.d1());
        assertNull(z.d1());

        io.move(z, "/abc/z");
        io.move(y, "/abc/y");
        io.move(x, "/abc/x");

        WnObj x2 = io.fetch(null, "/abc/x");
        WnObj y2 = io.fetch(null, "/abc/y");
        WnObj z2 = io.fetch(null, "/abc/z");

        assertEquals(x.id(), x2.id());
        assertEquals(y.id(), y2.id());
        assertEquals(z.id(), z2.id());
        assertEquals("abc", x2.d0());
        assertEquals("abc", y2.d0());
        assertEquals("abc", z2.d0());
        assertEquals("x", x2.d1());
        assertEquals("y", y2.d1());
        assertEquals("z", z2.d1());

        WnObj abc = io.check(null, "/abc");
        List<WnObj> list = io.query(Wn.Q.pid(abc).sortBy("nm", 1));

        assertEquals(3, list.size());
        assertEquals(x.id(), list.get(0).id());
        assertEquals(y.id(), list.get(1).id());
        assertEquals(z.id(), list.get(2).id());

    }

    @Test
    public void test_rename_chtype() {
        WnObj c = io.create(null, "/a/b/c", WnRace.FILE);
        WnObj o = io.rename(c, "xyz.js");

        assertEquals("xyz.js", o.name());
        assertEquals("/a/b/xyz.js", o.path());
        assertEquals("js", o.type());
        assertEquals(mimes().getMime("js"), o.mime());
    }

    @Test
    public void test_rename() {
        WnObj c = io.create(null, "/a/b/c", WnRace.FILE);
        WnObj o = io.rename(c, "xyz");

        assertEquals("xyz", o.name());
        assertEquals("/a/b/xyz", o.path());
    }

    @Test
    public void test_load_parents_twice() {
        WnObj c = io.create(null, "/a/b/c", WnRace.FILE);

        List<WnObj> l0 = new ArrayList<WnObj>(2);
        List<WnObj> l1 = new ArrayList<WnObj>(2);

        c.loadParents(l0, false);
        assertEquals(2, l0.size());
        assertEquals("a", l0.get(0).name());
        assertEquals("b", l0.get(1).name());

        c.loadParents(l1, false);
        assertEquals(2, l1.size());
        assertEquals("a", l1.get(0).name());
        assertEquals("b", l1.get(1).name());
    }

    @Test
    public void test_sync_time() throws InterruptedException {
        io.create(null, "/a/b/c/d", WnRace.FILE);
        WnObj b = io.check(null, "/a/b");

        // 同步时间不存在
        long st = b.syncTime();
        assertTrue(st <= 0);

        // 修改自身，同步时间还是不存在
        // io.changeType(b, "abc");
        io.appendMeta(b, "tp:'abc'");
        assertTrue(io.check(null, "/a/b").syncTime() <= 0);

        // 修改子节点，同步时间不存在
        WnObj c = io.check(b, "c");
        io.rename(c, "haha");
        assertTrue(io.check(null, "/a/b").syncTime() <= 0);

        // 修改孙文件，同步时间不存在
        WnObj d = io.check(c, "d");
        io.writeText(d, "hello");
        assertTrue(io.check(null, "/a/b").syncTime() <= 0);

        // 添加同步时间描述
        long last_st = Wn.now();
        io.appendMeta(b, "synt:" + last_st);

        // 等几毫秒 ...
        Thread.sleep(5);

        // 修改子节点，同步时间改变了
        io.rename(c, "c");
        st = io.check(null, "/a/b").syncTime();
        assertTrue(st > last_st);
        last_st = st;

        // 等几毫秒 ...
        Thread.sleep(5);

        // 修改孙文件，同步时间改变了
        io.writeText(d, "hello world");
        st = io.check(null, "/a/b").syncTime();
        assertTrue(st > last_st);
        last_st = st;

        // 等几毫秒 ...
        Thread.sleep(5);

        // 孙文件没改动，同步时间不改变
        io.writeText(d, "hello world");
        st = io.check(null, "/a/b").syncTime();
        assertTrue(st == last_st);

        // 等几毫秒 ...
        Thread.sleep(5);

        // 添加一个子节点，同步时间改变
        WnObj oNew = io.create(null, "/a/b/c/newfile", WnRace.FILE);
        st = io.check(null, "/a/b").syncTime();
        assertTrue(st > last_st);
        last_st = st;

        // 等几毫秒 ...
        Thread.sleep(5);

        // 删除一个子节点，同步时间改变
        io.delete(oNew);
        st = io.check(null, "/a/b").syncTime();
        assertTrue(st > last_st);
        last_st = st;

    }

    @Test
    public void test_mount_and_ln() {
        File f = Files.findFile("org/nutz/walnut");
        String aph = Files.getAbsPath(f);
        String mnt = "file://" + aph;

        /*
         * <pre> /a/b/ := org/nutz/walnut/core/bean/WnObjIdTest.class ^ |
         * +-----------link------------+ | /x/y/ </pre>
         */

        WnObj b = io.create(null, "/a/b", WnRace.DIR);
        WnObj y = io.create(null, "/x/y", WnRace.DIR);

        // 挂载
        io.setMount(b, mnt);

        // 链接
        y.link("/a/b/core/bean");
        io.set(y, "^ln$");

        // 试图找找文件
        String fnm = WnObjIdTest.class.getSimpleName() + ".class";
        WnObj x = io.fetch(null, "/x");

        // o => /x/y/WnObjIdTest.class
        WnObj o = io.fetch(x, "y/" + fnm);
        String oph = "/x/y/" + fnm;

        // 验证
        assertTrue(o.isMount());
        assertEquals(mnt, o.mount());
        assertEquals(fnm, o.name());
        assertEquals(oph, o.path());
        assertEquals("y", o.parent().name());
    }

    /**
     * mount 一个节点后，unmount 它，会回到原来的 mount
     */
    @Test
    public void test_mount_unmount() {
        WnObj b = io.create(null, "/a/b", WnRace.DIR);

        io.setMount(b, "file://~/noexists/dir");
        assertEquals("file://~/noexists/dir", b.mount());
        assertTrue(b.isMount());

        b = io.checkById(b.id());
        assertEquals("file://~/noexists/dir", b.mount());
        assertTrue(b.isMount());

        io.setMount(b, null);
        assertFalse(b.isMount());

        b = io.checkById(b.id());
        assertFalse(b.isMount());
    }

    @Test
    public void test_simple_link() {
        Wn.WC().setSecurity(new WnEvalLink(io));
        try {
            io.create(null, "/a/b/c/e/f", WnRace.FILE);
            WnObj z = io.create(null, "/x/y/z", WnRace.DIR);

            io.appendMeta(z, "ln:'/a/b'");

            WnObj o;

            o = io.fetch(null, "/x/y/z");
            assertEquals("z", o.name());
            assertEquals("/x/y/z", o.path());

            o = io.fetch(null, "/x/y/z/c");
            assertEquals("c", o.name());
            assertEquals("/x/y/z/c", o.path());

        }
        finally {
            Wn.WC().setSecurity(null);
        }

    }

    @Test
    public void test_read_write_link_file() throws Exception {
        WnObj a = io.create(null, "/linktest/a", WnRace.FILE);
        WnObj b = io.create(null, "/linktest/b", WnRace.FILE);
        try {
            io.writeText(a, "haha");
            io.appendMeta(b, "ln:'/linktest/a'");

            assertEquals("haha", io.readText(a));
            assertEquals("haha", io.readText(b));

            io.writeText(b, "dada");

            // 重新读取一下两个文件
            a = io.fetch(null, "/linktest/a");
            b = io.fetch(null, "/linktest/b");

            assertEquals("dada", io.readText(a));
            assertEquals("dada", io.readText(b));

        }
        finally {
            io.delete(a);
            io.delete(b);
        }
    }

    @Test
    public void test_read_write_link_dir() throws Exception {
        WnObj a = io.create(null, "/linktest/a", WnRace.DIR);
        WnObj a1 = io.create(null, "/linktest/a/a1", WnRace.FILE);
        WnObj b = io.create(null, "/linktest/b", WnRace.DIR);
        try {
            io.writeText(a1, "haha");
            io.appendMeta(b, "ln:'/linktest/a'");

            assertEquals("haha", io.readText(io.fetch(null, "/linktest/a/a1")));
            assertEquals("haha", io.readText(io.fetch(null, "/linktest/b/a1")));

            io.writeText(io.fetch(null, "/linktest/b/a1"), "hehe");

            assertEquals("hehe", io.readText(io.fetch(null, "/linktest/a/a1")));
            assertEquals("hehe", io.readText(io.fetch(null, "/linktest/b/a1")));

        }
        finally {
            io.delete(a1);
            io.delete(a);
            io.delete(b);
        }
    }

    // -------------------------------------------------------------
    // 测试类
    static class WalkTest implements Callback<WnObj> {
        StringBuilder sb;

        public void invoke(WnObj obj) {
            sb.append(obj.name());
        }

        public WalkTest reset() {
            sb = new StringBuilder();
            return this;
        }

        public String toString() {
            return sb.toString();
        }
    }

    // -------------------------------------------------------------

    @Test
    public void test_simple_create_right() {
        WnObj o = io.create(null, "/a", WnRace.DIR);
        String g = Wn.WC().checkMyGroup();
        String c = Wn.WC().checkMyName();

        assertEquals(g, o.group());
        assertEquals(c, o.mender());
        assertEquals(c, o.creator());
        assertEquals(0750, o.mode());
    }

    @Test
    public void test_get_top_dir() {
        io.create(null, "/a", WnRace.DIR);
        WnObj o = io.check(null, "/a");
        assertEquals("a", o.name());
    }

    @Test
    public void test_walk() {
        io.create(null, "/a/b/c", WnRace.FILE);
        io.create(null, "/a/b/d", WnRace.FILE);
        io.create(null, "/a/b/e", WnRace.FILE);
        io.create(null, "/a/x/y", WnRace.FILE);
        io.create(null, "/a/x/z", WnRace.FILE);

        WnObj a = io.check(null, "/a");

        WalkTest wt = new WalkTest();

        io.walk(a, wt.reset(), WalkMode.BREADTH_FIRST);
        assertEquals("bxcdeyz", wt.toString());

        io.walk(a, wt.reset(), WalkMode.DEPTH_NODE_FIRST);
        assertEquals("bcdexyz", wt.toString());

        io.walk(a, wt.reset(), WalkMode.DEPTH_LEAF_FIRST);
        assertEquals("cdebyzx", wt.toString());

        io.walk(a, wt.reset(), WalkMode.LEAF_ONLY);
        assertEquals("cdeyz", wt.toString());
    }

    @Test
    public void test_read_meta() {
        WnObj o = io.create(null, "/a", WnRace.FILE);
        io.appendMeta(o, "x:100, y:99");

        o = io.fetch(null, "/a");

        assertEquals(100, o.getInt("x"));
        assertEquals(99, o.getInt("y"));
    }

    @Test
    public void test_append_meta() {
        WnObj o = io.create(null, "/a", WnRace.FILE);
        io.appendMeta(o, "x:100, y:99");
        assertEquals(100, o.getInt("x"));
        assertEquals(99, o.getInt("y"));

        WnObj o2 = io.fetch(o, "/a");
        assertEquals(o.type(), o2.type());
        assertEquals(o.name(), o2.name());
        assertEquals(o.path(), o2.path());
        assertEquals(100, o2.getInt("x"));
        assertEquals(99, o2.getInt("y"));

        io.appendMeta(o, "{z:888}");
        assertEquals(100, o.getInt("x"));
        assertEquals(99, o.getInt("y"));
        assertEquals(888, o.getInt("z"));

        o2 = io.fetch(o, "/a");
        assertEquals(o.type(), o2.type());
        assertEquals(o.name(), o2.name());
        assertEquals(o.path(), o2.path());
        assertEquals(100, o2.getInt("x"));
        assertEquals(99, o2.getInt("y"));
        assertEquals(888, o2.getInt("z"));
    }

    @Test
    public void test_append_meta_rename() {
        WnObj o = io.create(null, "/a", WnRace.FILE);
        io.appendMeta(o, "nm:'b'");
        assertEquals("b", o.name());

        WnObj o2 = io.fetch(null, "/b");
        assertEquals("b", o2.name());
    }

    @Test
    public void test_write_meta() {
        WnObj o = io.create(null, "/a", WnRace.FILE);
        o.setv("x", 100).setv("y", 99);
        io.writeMeta(o, "^x|y$");
        assertEquals(100, o.getInt("x"));
        assertEquals(99, o.getInt("y"));

        WnObj o2 = io.fetch(o, "/a");
        assertEquals(o.type(), o2.type());
        assertEquals(o.name(), o2.name());
        assertEquals(o.path(), o2.path());
        assertEquals(100, o2.getInt("x"));
        assertEquals(99, o2.getInt("y"));
    }

    @Test
    public void test_write_meta2() {
        io.create(null, "/a", WnRace.FILE);
        WnObj o = io.fetch(null, "/a");
        io.writeMeta(o, "x:100,y:99");
        assertEquals(100, o.getInt("x"));
        assertEquals(99, o.getInt("y"));

        WnObj o2 = io.fetch(o, "/a");
        assertEquals(o.type(), o2.type());
        assertEquals(o.name(), o2.name());
        assertEquals(o.path(), o2.path());
        assertEquals(100, o2.getInt("x"));
        assertEquals(99, o2.getInt("y"));

        io.writeMeta(o, "{z:888}");
        assertEquals(-1, o.getInt("x"));
        assertEquals(-1, o.getInt("y"));
        assertEquals(888, o.getInt("z"));

        o2 = io.fetch(o, "/a");
        assertEquals(o.type(), o2.type());
        assertEquals(o.name(), o2.name());
        assertEquals(o.path(), o2.path());
        assertEquals(-1, o2.getInt("x"));
        assertEquals(-1, o2.getInt("y"));
        assertEquals(888, o2.getInt("z"));
    }

    @Test
    public void test_move() {
        WnObj o = io.create(null, "/a/b/c", WnRace.FILE);
        String oid = o.id();

        io.create(null, "/x/y", WnRace.DIR);
        io.move(o, "/x/y/z");
        assertEquals("z", o.name());
        assertEquals("/x/y/z", o.path());

        o = io.fetch(null, "/x/y/z");
        assertEquals(oid, o.id());
        assertEquals("z", o.name());
        assertEquals("/x/y/z", o.path());
    }

    @Test
    public void test_rewrite_content() {
        WnObj o = io.create(null, "/a/b/c", WnRace.FILE);

        io.writeText(o, "ABC");
        io.writeText(o, "A");

        assertEquals(1, o.len());
        assertEquals("A", io.readText(o));
        assertEquals(Lang.sha1("A"), o.sha1());
    }

    @Test
    public void test_simple_append() {
        WnObj o = io.create(null, "/a/b/c", WnRace.FILE);

        io.writeText(o, "A");
        io.appendText(o, "B");

        assertEquals(2, o.len());
        assertEquals(Lang.sha1("AB"), o.sha1());
        assertEquals("AB", io.readText(o));
    }

    @Test
    public void test_simple_create_rw_delete() {
        WnObj o = io.create(null, "/a/b/c", WnRace.FILE);

        assertEquals("/a/b/c", o.path());

        String str = "hello";
        io.writeText(o, str);
        String str2 = io.readText(o);

        assertEquals(str, str2);
        assertEquals(str.length(), o.len());
        assertEquals(Lang.sha1(str), o.sha1());

        // 验证一下数据库里记录的正确性
        o = io.fetch(null, "/a/b/c");
        assertEquals("/a/b/c", o.path());
        assertEquals(str, str2);
        assertEquals(str.length(), o.len());
        assertEquals(Lang.sha1(str), o.sha1());

        o = io.get(o.id());
        assertEquals(str.length(), o.len());
        assertEquals(Lang.sha1(str), o.sha1());

        // 删除
        io.delete(o);

        assertNull(io.fetch(null, "/a/b/c"));

        // 但是之前的目录还在
        WnObj a = io.fetch(null, "/a");
        WnObj b = io.fetch(a, "b");

        assertEquals(a.id(), b.parentId());
        assertEquals("/a", a.path());
        assertEquals("/a/b", b.path());
    }

    @Test
    public void test_random_write() throws IOException, WnIoHandleMutexException {
        WnObj o = io.create(null, "/a/b/c", WnRace.FILE);

        assertEquals("/a/b/c", o.path());

        io.writeText(o, "abc");
        String oldSha1 = o.sha1();

        WnIoHandle h = io.openHandle(o, Wn.S.WM);
        h.seek(1);
        h.write("z".getBytes());
        h.close();

        assertEquals("azc", io.readText(o));
        String newSha1 = o.sha1();

        // 重新读取，sha1 是新的
        o = io.fetch(null, "/a/b/c");
        assertEquals(newSha1, o.sha1());
        assertFalse(newSha1.equals(oldSha1));

        // 句柄应该全部都关闭了
        try {
            handles.check(h.getId());
            fail();
        }
        catch (Exception e) {}

        // 旧的 SHA1 已经没人引用了
        assertEquals(0, refers.count(oldSha1));

        // 新的 SHA1 有一个引用
        assertEquals(1, refers.count(newSha1));

        // 得到全局桶
        LocalIoBM bm = setup.getGlobalIoBM();

        // 旧的SHA1 文件被删掉了
        File fOld = bm.getBucketFile(oldSha1);
        assertFalse(fOld.exists());

        // 新的SHA1 文件存在
        File fNew = bm.getBucketFile(newSha1);
        assertTrue(fNew.exists());
    }

    @Test
    public void test_array_push_pull() throws IOException {
        WnObj o = io.create(null, "/a/b/c", WnRace.FILE);
        String id = o.id();

        assertEquals("/a/b/c", o.path());

        // 新增
        io.push(id, "pets", "wendal", false);
        io.push(id, "pets", "zozoh", false);
        io.push(id, "pets", "pangwu", false);

        o = io.get(id);
        assertNotNull(o);
        assertNotNull(o.get("pets"));
        assertEquals(3, Lang.eleSize(o.get("pets")));
        Lang.each(o.get("pets"), new Each<Object>() {
            public void invoke(int index, Object ele, int length) {
                assertTrue("wendal".equals(ele) || "zozoh".equals(ele) || "pangwu".equals(ele));
            }
        });

        // 减少
        io.pull(id, "pets", "zozoh", false);
        o = io.get(id);
        assertNotNull(o);
        assertNotNull(o.get("pets"));
        assertEquals(2, Lang.eleSize(o.get("pets")));
        Lang.each(o.get("pets"), new Each<Object>() {
            public void invoke(int index, Object ele, int length) {
                assertTrue("wendal".equals(ele) || "pangwu".equals(ele));
            }
        });

        io.pull(id, "pets", "wendal", false);
        io.pull(id, "pets", "pangwu", false);

        o = io.get(id);
        assertNotNull(o);
        assertEquals(0, Lang.eleSize(o.get("pets")));

    }

    @Test
    public void test_array_push_pull_by_query() throws IOException {
        io.create(null, "/a/b/z", WnRace.FILE);
        WnObj o = io.create(null, "/a/b/c", WnRace.FILE);
        String id = o.id();
        String pid = o.parentId();

        assertEquals("/a/b/c", o.path());

        // 新增
        io.push(Wn.Q.id(id), "pets", "wendal");
        io.push(Wn.Q.pid(pid), "pets", "zozoh");
        io.push(Wn.Q.pid(pid), "pets", "pangwu");

        o = io.get(id);
        assertNotNull(o);
        assertNotNull(o.get("pets"));
        assertEquals(3, Lang.eleSize(o.get("pets")));
        Lang.each(o.get("pets"), new Each<Object>() {
            public void invoke(int index, Object ele, int length) {
                assertTrue("wendal".equals(ele) || "zozoh".equals(ele) || "pangwu".equals(ele));
            }
        });

        // 减少
        io.pull(Wn.Q.pid(pid), "pets", "zozoh");
        o = io.get(id);
        assertNotNull(o);
        assertNotNull(o.get("pets"));
        assertEquals(2, Lang.eleSize(o.get("pets")));
        Lang.each(o.get("pets"), new Each<Object>() {
            public void invoke(int index, Object ele, int length) {
                assertTrue("wendal".equals(ele) || "pangwu".equals(ele));
            }
        });

        io.pull(Wn.Q.pid(pid), "pets", "wendal");
        io.pull(Wn.Q.pid(pid), "pets", "pangwu");

        o = io.get(id);
        assertNotNull(o);
        assertEquals(0, Lang.eleSize(o.get("pets")));

    }

    @Test
    public void test_mount_local_mime() {
        WnObj x = io.create(null, "/x", WnRace.DIR);
        try {
            File f = Files.createFileIfNoExists2("~/.walnut/tmp/css/my.css");
            io.setMount(x, "file://~/.walnut/tmp/css");
            WnObj o = io.check(null, "/x/my.css");
            assertEquals("my.css", o.name());
            assertEquals("css", o.type());
            assertEquals("text/css", o.mime());
            assertEquals(f.length(), o.len());
        }
        finally {
            Files.deleteDir(Files.findFile("~/.walnut/tmp"));
        }
    }

}
