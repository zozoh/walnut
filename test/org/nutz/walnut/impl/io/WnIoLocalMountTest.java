package org.nutz.walnut.impl.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.nutz.lang.Files;
import org.nutz.walnut.BaseIoTest;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnContext;

public class WnIoLocalMountTest extends BaseIoTest {

    /**
     * for issue #17
     */
    @Test
    public void test_mount_local_auto_use_root_cgm() {
        WnObj b = io.create(null, "/a/b", WnRace.DIR);

        // 创建临时目录
        try {
            Files.createFileIfNoExists2("~/.walnut/tmp/dir/x/y.txt");

            // 挂载目录
            WnContext wc = Wn.WC();
            String me = wc.checkMe();
            String grp = wc.checkGroup();
            wc.me("nobody", "nogrp");
            try {
                io.setMount(b, "file://~/.walnut/tmp/dir");

                // 获取
                WnObj o = io.check(null, "/a/b/x/y.txt");
                assertEquals("y.txt", o.name());
                assertTrue(o.isFILE());
                assertEquals(me, o.creator());
                assertEquals(me, o.mender());
                assertEquals(grp, o.group());

                o = io.check(null, "/a/b/x");
                assertEquals("x", o.name());
                assertTrue(o.isDIR());
                assertEquals(me, o.creator());
                assertEquals(me, o.mender());
                assertEquals(grp, o.group());

                o = io.check(null, "/a/b");
                assertEquals("b", o.name());
                assertTrue(o.isDIR());
                assertEquals(me, o.creator());
                assertEquals(me, o.mender());
                assertEquals(grp, o.group());

            }
            finally {
                wc.me(me, grp);
            }
        }
        // 删除临时目录
        finally {
            Files.deleteDir(Files.findFile("~/.walnut/tmp"));
        }
    }

}
