package com.site0.walnut.impl.io;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;
import org.nutz.lang.Files;
import com.site0.walnut.BaseIoTest;
import com.site0.walnut.api.auth.WnAccount;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.WnContext;
import org.nutz.web.WebException;

public class LocalMappingTest extends BaseIoTest {

    @Override
    protected void on_before() {}

    @Override
    protected void on_after() {}

    @Test
    public void test_write_to_local() {
        WnObj x = io.create(null, "/x", WnRace.DIR);
        File dHome = setup.getLocalFileHome();
        String aph = Files.getAbsPath(dHome);

        // 映射到本地
        io.setMount(x, "filew://" + aph);

        // 创建一个文件
        WnObj o = io.create(x, "abc.txt", WnRace.FILE);

        // 写入内容
        io.writeText(o, "hello");

        // 读取内容
        String text = io.readText(o);
        assertEquals("hello", text);

        // 确保本地文件也是这个
        File f = Files.getFile(dHome, "abc.txt");
        assertTrue(f.exists());
        text = Files.read(f);
        assertEquals("hello", text);
    }

    @Test
    public void test_mount_local_mime() {
        WnObj x = io.create(null, "/x", WnRace.DIR);

        File dHome = setup.getLocalFileHome();
        String aph = Files.getAbsPath(dHome);
        File f = Files.getFile(dHome, "tmp/css/my.css");
        Files.createFileIfNoExists(f);
        io.setMount(x, "file://" + aph + "/tmp/css");
        WnObj o = io.check(null, "/x/my.css");
        assertEquals("my.css", o.name());
        assertEquals("css", o.type());
        assertEquals("text/css", o.mime());
        assertEquals(f.length(), o.len());

    }

    @Test
    public void test_get_parent_as_root() {
        WnObj x = io.create(null, "/x", WnRace.DIR);

        File dHome = setup.getLocalFileHome();
        String aph = Files.getAbsPath(dHome);
        File f = Files.getFile(dHome, "tmp/css/my.css");
        Files.createFileIfNoExists(f);
        io.setMount(x, "file://" + aph + "/tmp/css");

        WnObj o = io.check(null, "/x/my.css");

        WnObj p = o.parent();
        assertEquals(x.id(), p.id());
    }

    /**
     * for issue #17
     */
    @Test
    public void test_mount_local_auto_use_root_cgm() {
        WnObj b = io.create(null, "/a/b", WnRace.DIR);

        // 创建临时文件并写点内容
        File dHome = setup.getLocalFileHome();
        String aph = Files.getAbsPath(dHome);
        File f = Files.getFile(dHome, "tmp/dir/x/y.txt");
        Files.write(f, "hello");

        // 挂载目录
        WnContext wc = Wn.WC();
        WnAccount me = wc.getMe();
        String myName = wc.getMyName();
        String myGroup = wc.getMyGroup();

        // 顺便测测上下文的帮助函数
        assertEquals(me.getName(), myName);
        assertEquals(me.getGroupName(), myGroup);

        // 切换上下文用户为新用户
        WnAccount nobody = WnAccount.createByHost("nobody", "nogrp");
        wc.setMe(nobody);
        try {
            io.setMount(b, "file://" + aph + "/tmp/dir");

            // 获取
            WnObj o = io.check(null, "/a/b/x/y.txt");
            assertEquals("y.txt", o.name());
            assertTrue(o.isFILE());
            assertEquals(myName, o.creator());
            assertEquals(myName, o.mender());
            assertEquals(myGroup, o.group());

            o = io.check(null, "/a/b/x");
            assertEquals("x", o.name());
            assertTrue(o.isDIR());
            assertEquals(myName, o.creator());
            assertEquals(myName, o.mender());
            assertEquals(myGroup, o.group());

            o = io.check(o, "y.txt");
            assertEquals("y.txt", o.name());
            assertTrue(o.isFILE());
            assertEquals(myName, o.creator());
            assertEquals(myName, o.mender());
            assertEquals(myGroup, o.group());

            o = io.check(null, "/a/b");
            assertEquals("b", o.name());
            assertTrue(o.isDIR());
            assertEquals(myName, o.creator());
            assertEquals(myName, o.mender());
            assertEquals(myGroup, o.group());

            // 读取
            o = io.check(null, "/a/b/x/y.txt");
            String str = io.readText(o);
            assertEquals("hello", str);

            // 只能读不能写
            try {
                io.writeText(o, "I am great!");
                fail();
            }
            catch (WebException e) {}

        }
        finally {
            wc.setMe(me);
        }

    }

}
