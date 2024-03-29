package com.site0.walnut.impl.hook;

import static org.junit.Assert.*;

import org.junit.Test;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.Strings;
import org.nutz.trans.Atom;
import org.nutz.trans.Proton;
import com.site0.walnut.BaseHookTest;
import com.site0.walnut.api.box.WnBox;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.util.Wn;

public class IoHookTest extends BaseHookTest {

    @Test
    public void test_mount() {
        // 准备钩子
        WnObj oHook = io.createIfNoExists(oHookHome, "mount/do_log", WnRace.FILE);
        io.writeText(oHook, "echo '${nm} mnt:[${mnt}] old:[${_mnt_from}]' >> ~/testlog");

        // 执行
        Wn.WC().hooking(hc, new Atom() {
            public void run() {
                WnObj o = io.create(oHome, "mydir", WnRace.DIR);
                io.setMount(o, "file://~/tmp/walnut");
                io.setMount(o, null);
            }
        });

        // 查看 log
        WnObj oLog = io.check(oHome, "testlog");
        String log = io.readText(oLog);
        String[] lines = Strings.splitIgnoreBlank(log, "\n");
        assertEquals(2, lines.length);
        assertEquals("mydir mnt:[file://~/tmp/walnut] old:[]", lines[0]);
        assertEquals("mydir mnt:[] old:[file://~/tmp/walnut]", lines[1]);
    }

    @Test
    public void test_meta() {
        // 准备钩子
        WnObj oHook = io.createIfNoExists(oHookHome, "meta/do_log", WnRace.FILE);
        io.writeText(oHook, "obj -VN id:${id} -c -e '^x|y|z$' >> ~/testlog");

        // 执行
        Wn.WC().hooking(hc, new Atom() {
            public void run() {
                WnObj o = io.create(oHome, "abc.js", WnRace.FILE);
                o.put("__debug_hook", true);
                io.appendMeta(o, "x:19");
                io.appendMeta(o, "y:89");
                io.appendMeta(o, "z:64");
            }
        });

        // 查看 log
        WnObj oLog = io.check(oHome, "testlog");
        String log = io.readText(oLog);
        String[] lines = Strings.splitIgnoreBlank(log, "\n");
        assertEquals(3, lines.length);
        assertEquals("19", lines[0]);
        assertEquals("1989", lines[1]);
        assertEquals("198964", lines[2]);
    }

    @Test
    public void test_move() {
        // 准备钩子
        WnObj oHook = io.createIfNoExists(oHookHome, "before_move/do_log", WnRace.FILE);
        io.writeText(oHook, "echo 'mv:${nm} to ${_mv_to}' >> ~/testlog");

        // 执行
        Wn.WC().hooking(hc, new Atom() {
            public void run() {
                WnObj o = io.create(oHome, "abc.js", WnRace.FILE);
                io.move(o, o.path() + ".new.js");

                o = io.create(oHome, "bbc.js", WnRace.FILE);
                io.move(o, o.path() + ".new.js");

                o = io.create(oHome, "cbc.js", WnRace.FILE);
                io.move(o, o.path() + ".new.js");
            }
        });

        // 查看 log
        WnObj oLog = io.check(oHome, "testlog");
        String log = io.readText(oLog);
        String[] lines = Strings.splitIgnoreBlank(log, "\n");
        assertEquals(3, lines.length);
        assertEquals("mv:abc.js to /home/xiaobai/abc.js.new.js", lines[0]);
        assertEquals("mv:bbc.js to /home/xiaobai/bbc.js.new.js", lines[1]);
        assertEquals("mv:cbc.js to /home/xiaobai/cbc.js.new.js", lines[2]);
    }

    @Test
    public void test_create_in_case() {
        // 准备钩子
        WnObj oHook = io.createIfNoExists(oHookHome, "create/for_js", WnRace.FILE);
        io.writeText(oHook, "echo 'js:${nm}' >> ~/testlog");
        io.appendMeta(oHook, "hook_by:[{tp:'js'}]");

        oHook = io.createIfNoExists(oHookHome, "create/for_css", WnRace.FILE);
        io.writeText(oHook, "echo 'css:${nm}' >> ~/testlog");
        io.appendMeta(oHook, "hook_by:[{tp:'css'}]");

        // 执行
        Wn.WC().hooking(hc, new Atom() {
            public void run() {
                io.create(oHome, "abc.js", WnRace.FILE);
                io.create(oHome, "abc.css", WnRace.FILE);
                io.create(oHome, "abc", WnRace.FILE);
                io.create(oHome, "last.js", WnRace.FILE);
            }
        });

        // 查看 log
        WnObj oLog = io.check(oHome, "testlog");
        String log = io.readText(oLog);
        String[] lines = Strings.splitIgnoreBlank(log, "\n");
        assertEquals(3, lines.length);
        assertEquals("js:abc.js", lines[0]);
        assertEquals("css:abc.css", lines[1]);
        assertEquals("js:last.js", lines[2]);

    }

    @Test
    public void test_delete_in_cmd_pipe() {
        // 准备钩子
        WnObj oHook = io.createIfNoExists(oHookHome, "before_delete/cp_backup", WnRace.FILE);
        io.writeText(oHook, "cp ${ph} ${ph}.bak");

        // 准备素材
        final WnObj o = io.create(oHome, "abc.txt", WnRace.FILE);
        io.writeText(o, "hello");

        // 执行创建
        Wn.WC().hooking(hc, new Atom() {
            public void run() {
                WnBox box = _alloc_box();
                box.run("rm ~/abc.txt | echo DONE");
                boxes.free(box);
            }
        });

        // 验证
        assertNull(io.fetch(null, o.path()));
        WnObj oBak = io.check(oHome, "abc.txt.bak");
        String txt = io.readText(oBak);
        assertEquals("hello", txt);
        assertFalse(o.isSameId(oBak));
    }

    @Test
    public void test_delete_in_cmd() {
        // 准备钩子
        WnObj oHook = io.createIfNoExists(oHookHome, "before_delete/cp_backup", WnRace.FILE);
        io.writeText(oHook, "cp ${ph} ${ph}.bak");

        // 准备素材
        final WnObj o = io.create(oHome, "abc.txt", WnRace.FILE);
        io.writeText(o, "hello");

        // 执行创建
        Wn.WC().hooking(hc, new Atom() {
            public void run() {
                WnBox box = _alloc_box();
                box.run("rm ~/abc.txt");
                boxes.free(box);
            }
        });

        // 验证
        assertNull(io.fetch(null, o.path()));
        WnObj oBak = io.check(oHome, "abc.txt.bak");
        String txt = io.readText(oBak);
        assertEquals("hello", txt);
        assertFalse(o.isSameId(oBak));
    }

    @Test
    public void test_delete() {
        // 准备钩子
        WnObj oHook = io.createIfNoExists(oHookHome, "before_delete/cp_backup", WnRace.FILE);
        io.writeText(oHook, "cp ${ph} ${ph}.bak");

        // 准备素材
        final WnObj o = io.create(oHome, "abc.txt", WnRace.FILE);
        io.writeText(o, "hello");

        // 执行创建
        Wn.WC().hooking(hc, new Atom() {
            public void run() {
                io.delete(o);
            }
        });

        // 验证
        assertNull(io.fetch(null, o.path()));
        WnObj oBak = io.check(oHome, "abc.txt.bak");
        String txt = io.readText(oBak);
        assertEquals("hello", txt);
        assertFalse(o.isSameId(oBak));
    }

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
        assertEquals(Wlang.md5("hello\n") + "\n" + Wlang.sha1(Wlang.md5("hello\n") + "\n") + "\n",
                     txt);
        assertEquals(Wlang.sha1(txt), o.sha1());
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
        assertEquals(Wlang.md5("/home/xiaobai/abc.txt\n") + "\n", txt);
        assertEquals(Wlang.sha1(txt), o.sha1());
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
        assertEquals(Wlang.md5("/home/xiaobai/abc.txt\n") + "\n", txt);
        assertEquals(Wlang.sha1(txt), o.sha1());
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
        assertEquals(Wlang.sha1(txt), o.sha1());
    }

}
