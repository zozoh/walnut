package org.nutz.walnut.impl.io;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;
import org.nutz.lang.Files;
import org.nutz.walnut.BaseIoTest;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnContext;
import org.nutz.web.WebException;

public class TestWnIoLocalMount extends BaseIoTest {

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

    /**
     * for issue #17
     */
    @Test
    public void test_mount_local_auto_use_root_cgm() {
        WnObj b = io.create(null, "/a/b", WnRace.DIR);

        try {
            // 创建临时文件并写点内容
            File f = Files.createFileIfNoExists2("~/.walnut/tmp/dir/x/y.txt");
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
            WnAccount nobody = WnAccount.create("nobody", "nogrp");
            wc.setMe(nobody);
            try {
                io.setMount(b, "file://~/.walnut/tmp/dir");

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
        // 删除临时目录
        finally {
            Files.deleteDir(Files.findFile("~/.walnut/tmp"));
        }
    }

}
