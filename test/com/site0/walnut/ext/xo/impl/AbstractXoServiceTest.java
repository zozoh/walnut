package com.site0.walnut.ext.xo.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.Test;
import org.nutz.json.Json;
import org.nutz.lang.Streams;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.BaseSessionTest;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.ext.xo.bean.XoBean;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.Wsum;

public abstract class AbstractXoServiceTest extends BaseSessionTest {

    @Override
    protected void on_before() {
        super.on_before();
    }

    protected XoService service() {
        return service(null, null);
    }

    protected XoService service(String prefix) {
        return service(prefix, null);
    }

    protected abstract String getConfCateName();

    protected XoService service(String prefix, String[] allowActions) {
        // 准备域目录
        WnObj oIo = io.createIfNoExists(oMyHome, ".io", WnRace.DIR);
        io.createIfNoExists(oMyHome, ".domain/xo_token", WnRace.DIR);

        // 创建配置文件
        String cateName = getConfCateName();
        String confName = "test";
        if (!Ws.isBlank(prefix)) {
            confName += "_" + prefix.replaceAll("/", "");
        }
        WnObj oConf = io
            .create(oIo, cateName + "/" + confName + ".json5", WnRace.FILE);

        // 写入配置文件内容
        NutMap conf = new NutMap();
        update_config(prefix, allowActions, conf);

        String json = Json.toJson(conf);
        io.writeText(oConf, json);

        // 准备服务类
        return create_service(confName);
    }

    protected abstract void update_config(String prefix,
                                          String[] allowActions,
                                          NutMap conf);

    protected abstract XoService create_service(String confName);

    // @Test
    public void test_zwrite_big_file() throws IOException {
        XoService api = service("video/");

        // 首先清除数据
        api.clear("*");

        // 获取大文件
        String fph = setup.getConifg("xo-big-file");
        File f = new File(fph);
        String md51 = Wsum.md5AsString(f);
        InputStream ins = Streams.fileIn(f);
        String fkey = "视频素材/" + f.getName();
        api.write(fkey, ins, null);

        // 读取大文件内容
        byte[] bs = api.readBytes(fkey);
        String md52 = Wsum.md5AsString(bs);
        assertEquals(md51, md52);
    }

    @Test
    public void test_x00() throws IOException {
        XoService api = service("pet/");

        // 首先清除数据
        api.clear("*");

        // 创建一个文件
        api.writeText("dog/wangcai.txt", "I am wangcai", null);

        // 测试虚拟文件夹
        List<XoBean> list = api.listObj("dog/wangcai.txt");
        assertEquals(1, list.size());
        XoBean dog = list.get(0);
        assertEquals("dog/wangcai.txt", dog.getKey());
        assertEquals("wangcai.txt", dog.getName());
        assertFalse(dog.isVirtual());
        assertTrue(dog.isFILE());
    }

    @Test
    public void test_y01() throws IOException {
        XoService api = service("pet/");

        // 首先清除数据
        api.clear("*");

        // 创建一个文件
        api.mkdir("a/b/c");

        // 重命名
        api.renameKey("a/b/c/", "d");

        // 读取元数据
        XoBean xo = api.getObj("a/b/d/");
        assertEquals("a/b/d/", xo.getKey());
        assertEquals("d", xo.getName());

        // 测试虚拟文件夹
        List<XoBean> list = api.listObj("a/");
        assertEquals(1, list.size());
        assertEquals("a/b/", list.get(0).getKey());
        assertEquals("b", list.get(0).getName());
        assertTrue(list.get(0).isVirtual());
        assertTrue(list.get(0).isDIR());

    }

    @Test
    public void test_y00() throws IOException {
        XoService api = service();

        // 首先清除数据
        api.clear("*");

        // 创建一个文件
        api.writeText("abc/xx.txt", "I am xx", null);
        api.writeText("abc/qq.txt", "I am QQ", null);

        // 重命名
        api.renameKey("abc/xx.txt", "yy.txt");

        // 读取内容
        String str = api.readText("abc/yy.txt");
        assertEquals("I am xx", str);

        // 读取元数据
        XoBean xo = api.getObj("abc/yy.txt");
        assertEquals("abc/yy.txt", xo.getKey());
        assertEquals("yy.txt", xo.getName());
        assertFalse(xo.isVirtual());
        assertTrue(xo.isFILE());

        // 测试虚拟文件夹
        xo = api.getObj("abc/");
        assertNull(xo);

        // 测试虚拟文件夹
        List<XoBean> list = api.listObj("abc/");
        assertEquals(2, list.size());
        assertEquals("abc/qq.txt", list.get(0).getKey());
        assertEquals("qq.txt", list.get(0).getName());
        assertFalse(list.get(0).isVirtual());
        assertTrue(list.get(0).isFILE());

        assertEquals("abc/yy.txt", list.get(1).getKey());
        assertEquals("yy.txt", list.get(1).getName());
        assertFalse(list.get(1).isVirtual());
        assertTrue(list.get(1).isFILE());

    }

    @Test
    public void test_z05() throws IOException {
        XoService api = service("pet/");

        // 首先清除数据
        api.clear("*");

        // 创建一个文件
        api.writeText("abc/xx.txt", "I am xx", null);

        // 重命名
        api.renameKey("abc/xx.txt", "yy.txt");

        // 读取内容
        String str = api.readText("abc/yy.txt");
        assertEquals("I am xx", str);

        // 读取元数据
        XoBean xo = api.getObj("abc/yy.txt");
        assertEquals("abc/yy.txt", xo.getKey());
        assertEquals("yy.txt", xo.getName());
    }

    @Test
    public void test_z04() throws IOException {
        XoService api = service("folder/");

        // 首先清除数据
        api.clear("*");

        // 带着元数据写入
        api.mkdir("aaa/bbb/ccc");

        List<XoBean> list = api.listObj("*", false, 100);
        assertEquals(1, list.size());
        assertEquals("aaa/bbb/ccc/", list.get(0).getKey());

        list = api.listObj("aaa/*");
        assertEquals(1, list.size());
        assertEquals("aaa/bbb/", list.get(0).getKey());
        assertTrue(list.get(0).isVirtual());

        list = api.listObj("aaa/");
        assertEquals(1, list.size());
        assertEquals("aaa/bbb/", list.get(0).getKey());
        assertTrue(list.get(0).isVirtual());

        list = api.listObj("aaa/bbb/*");
        assertEquals(1, list.size());
        assertEquals("aaa/bbb/ccc/", list.get(0).getKey());
        assertTrue(list.get(0).isVirtual());

        list = api.listObj("aaa/bbb/ccc/");
        assertEquals(0, list.size());

        XoBean xo = api.getObj("aaa/bbb/ccc/");
        assertEquals("aaa/bbb/ccc/", xo.getKey());
        assertTrue(xo.isDIR());
        assertFalse(xo.isVirtual());

    }

    @Test
    public void test_z03() throws IOException {
        XoService api = service("pet/");

        String key = "a.dog.txt";

        // 首先清除数据
        api.clear("pet/");

        // 带着元数据写入
        api.writeText(key,
                      "this is a dog",
                      Wlang.map("x:100,y:99,name:'hello'"));

        // 读取对象
        XoBean xo = api.getObj(key);
        assertEquals(100, xo.userMeta().getInt("x"));
        assertEquals(99, xo.userMeta().getInt("y"));
        assertEquals("hello", xo.userMeta().getString("name"));
        assertEquals(3, xo.userMeta().size());

        // 更新
        api.appendMeta(key, Wlang.map("x:88"));

        xo = api.getObj(key);
        assertEquals(88, xo.userMeta().getInt("x"));
        assertEquals(99, xo.userMeta().getInt("y"));
        assertEquals("hello", xo.userMeta().getString("name"));
        assertEquals(3, xo.userMeta().size());

        // 再次更新，并删除某个键
        api.appendMeta(key, Wlang.map("x:0,name:null"));

        xo = api.getObj(key);
        assertEquals(0, xo.userMeta().getInt("x"));
        assertEquals(99, xo.userMeta().getInt("y"));
        assertNull(xo.userMeta().get("name"));
        assertEquals(2, xo.userMeta().size());
    }

    @Test
    public void test_z02() throws IOException {
        XoService api = service();

        String key = "pet/a.pet.txt";

        // 首先清除数据
        api.clear("pet/");

        List<XoBean> list = api.listObj("pet/");
        assertEquals(0, list.size());

        // 创建
        api.writeText(key, "I am A", null);

        // 查询
        list = api.listObj("pet/");
        assertEquals(1, list.size());

        // 读取
        String str = api.readText(key);
        assertEquals("I am A", str);

        // 覆盖
        api.writeText(key, "I am B", null);
        str = api.readText(key);
        assertEquals("I am B", str);

        // 删除
        api.deleteObj(key);

        // 确保删除了
        list = api.listObj("pet/");
        assertEquals(0, list.size());
    }

    @Test
    public void test_z01() throws IOException {
        XoService api = service("pet/");

        String key = "a.pet.txt";

        // 首先清除数据
        api.clear("*");

        List<XoBean> list = api.listObj("*");
        assertEquals(0, list.size());

        // 创建
        api.writeText(key, "I am A", null);

        // 查询
        list = api.listObj("*");
        assertEquals(1, list.size());
        assertEquals(key, list.get(0).getKey());

        // 读取
        String str = api.readText(key);
        assertEquals("I am A", str);

        // 覆盖
        api.writeText(key, "I am B", null);
        str = api.readText(key);
        assertEquals("I am B", str);

        // 删除
        api.deleteObj(key);

        // 确保删除了
        list = api.listObj(null);
        assertEquals(0, list.size());
    }

    @Test
    public void test_z00() throws IOException {
        XoService api = service("pet/");

        String key1 = "a/pet1.txt";
        String key2 = "a/pet2.txt";
        String key3 = "a/pet3.txt";

        // 首先清除数据
        api.clear("*");

        List<XoBean> list = api.listObj("*");
        assertEquals(0, list.size());

        // 创建
        api.writeText(key1, "I am A", null);
        api.writeText(key2, "I am B", null);
        api.writeText(key3, "I am C", null);

        // 查询
        list = api.listObj("a/");
        assertEquals(3, list.size());
        assertEquals(key1, list.get(0).getKey());
        assertEquals(key2, list.get(1).getKey());
        assertEquals(key3, list.get(2).getKey());

        // 读取
        String str1 = api.readText(key1);
        assertEquals("I am A", str1);
        String str2 = api.readText(key2);
        assertEquals("I am B", str2);
        String str3 = api.readText(key3);
        assertEquals("I am C", str3);
    }

}
