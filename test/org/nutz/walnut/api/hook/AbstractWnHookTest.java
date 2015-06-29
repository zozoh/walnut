package org.nutz.walnut.api.hook;

import static org.junit.Assert.*;

import org.junit.Test;
import org.nutz.lang.Lang;
import org.nutz.trans.Atom;
import org.nutz.trans.Proton;
import org.nutz.walnut.BaseHookTest;
import org.nutz.walnut.api.box.WnBox;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.util.Wn;

public abstract class AbstractWnHookTest extends BaseHookTest {

    @Test
    public void test_create_in_cmd_pipe() {
        // 准备钩子
        WnObj oHook = io.createIfNoExists(oHookHome, "write/append_sha1", WnRace.FILE);
        io.writeText(oHook, "echo '${sha1}' >> id:${id}");

        // 执行创建
        Wn.WC().hooking(hc, new Atom() {
            public void run() {
                WnBox box = _alloc_box();
                box.run("echo 'hello' | md5sum > ~/abc.txt");
                boxes.free(box);
            }
        });

        // 验证
        WnObj o = io.check(oHome, "abc.txt");
        String txt = io.readText(o);
        assertEquals(Lang.md5("hello\n") + "\n" + Lang.sha1("b1946ac92492d2347c6235b4d2611184\n") + "\n", txt);
        assertEquals(Lang.sha1(txt), o.sha1());
    }

    @Test
    public void test_create_in_cmd() {
        // 准备钩子
        WnObj oHook = io.createIfNoExists(oHookHome, "create/show_ph", WnRace.FILE);
        io.writeText(oHook, "echo '${ph}' | md5sum > id:${id}\n");

        // 执行创建
        Wn.WC().hooking(hc, new Atom() {
            public void run() {
                box.run("touch ~/abc.txt");
            }
        });

        // 验证
        WnObj o = io.check(oHome, "abc.txt");
        String txt = io.readText(o);
        assertEquals(Lang.md5("/home/xiaobai/abc.txt\n") + "\n", txt);
        assertEquals(Lang.sha1(txt), o.sha1());
    }

    @Test
    public void test_create2() {
        // 准备钩子
        WnObj oHook = io.createIfNoExists(oHookHome, "create/show_ph", WnRace.FILE);
        io.writeText(oHook, "echo '${ph}' | md5sum > id:${id}\n");

        // 执行创建
        WnObj o = Wn.WC().hooking(hc, new Proton<WnObj>() {
            protected WnObj exec() {
                return io.create(oHome, "abc.txt", WnRace.FILE);
            }
        });

        // 验证
        String txt = io.readText(o);
        assertEquals(Lang.md5("/home/xiaobai/abc.txt\n") + "\n", txt);
        assertEquals(Lang.sha1(txt), o.sha1());
    }

    @Test
    public void test_create() {
        // 准备钩子
        WnObj oHook = io.createIfNoExists(oHookHome, "create/show_ph", WnRace.FILE);
        io.writeText(oHook, "echo '${ph}' > id:${id}\n");

        // 执行创建
        WnObj o = Wn.WC().hooking(hc, new Proton<WnObj>() {
            protected WnObj exec() {
                return io.create(oHome, "abc.txt", WnRace.FILE);
            }
        });

        // 验证
        String txt = io.readText(o);
        assertEquals("/home/xiaobai/abc.txt\n", txt);
        assertEquals(Lang.sha1(txt), o.sha1());
    }

}
