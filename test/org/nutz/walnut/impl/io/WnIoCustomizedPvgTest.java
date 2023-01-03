package org.nutz.walnut.impl.io;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.core.IoCoreTest;
import org.nutz.walnut.util.Wlang;
import org.nutz.walnut.util.Wn;

public class WnIoCustomizedPvgTest extends IoCoreTest {

    private WnIo io;

    @Before
    public void setUp() throws Exception {
        this.setup.cleanAllData();
        this.io = this.setup.getIo();

    }

    @Test
    public void test_join_strong() {
        WnObj a = io.create(null, "/a", WnRace.DIR);
        WnObj b = io.create(a, "b", WnRace.DIR);
        WnObj c = io.create(b, "c", WnRace.FILE);

        a.put("pvg", Wlang.map("{'@[xx]':'0750'}"));
        b.put("pvg", Wlang.map("{'@[zz]':'!0755'}"));
        c.put("pvg", Wlang.map("{'@[zz]':'0777'}"));

        WnAccount x = setup.genAccount("xx");
        WnAccount y = setup.genAccount("yy");
        WnAccount z = setup.genAccount("zz");

        int pvg;

        pvg = c.getCustomizedPrivilege(x, 0);
        assertEquals(Wn.Io.modeFromOctal("0750"), pvg);

        pvg = c.getCustomizedPrivilege(y, 0);
        assertEquals(0, pvg);

        pvg = c.getCustomizedPrivilege(z, 0);
        assertEquals(Wn.Io.modeFromOctal("0755"), pvg);

    }

    @Test
    public void test_join_weak() {
        WnObj a = io.create(null, "/a", WnRace.DIR);
        WnObj b = io.create(a, "b", WnRace.DIR);
        WnObj c = io.create(b, "c", WnRace.FILE);

        a.put("pvg", Wlang.map("{'@[xx]':'0750'}"));
        b.put("pvg", Wlang.map("{'@[yy]':'~0755'}"));
        c.put("pvg", Wlang.map("{'@[zz]':'0777'}"));

        WnAccount x = setup.genAccount("xx");
        WnAccount y = setup.genAccount("yy");
        WnAccount z = setup.genAccount("zz");

        int pvg;

        pvg = c.getCustomizedPrivilege(x, 0);
        assertEquals(Wn.Io.modeFromOctal("0750"), pvg);

        pvg = c.getCustomizedPrivilege(y, 0);
        assertEquals(0, pvg);

        pvg = c.getCustomizedPrivilege(z, 0);
        assertEquals(Wn.Io.modeFromOctal("0777"), pvg);

    }

    @Test
    public void test_join_default() {
        WnObj a = io.create(null, "/a", WnRace.DIR);
        WnObj b = io.create(a, "b", WnRace.DIR);
        WnObj c = io.create(b, "c", WnRace.FILE);

        a.put("pvg", Wlang.map("{'@[xx]':'0750'}"));
        b.put("pvg", Wlang.map("{'@[yy]':'0755'}"));
        c.put("pvg", Wlang.map("{'@[zz]':'0777'}"));

        WnAccount x = setup.genAccount("xx");
        WnAccount y = setup.genAccount("yy");
        WnAccount z = setup.genAccount("zz");

        int pvg;

        pvg = c.getCustomizedPrivilege(x, 0);
        assertEquals(Wn.Io.modeFromOctal("0750"), pvg);

        pvg = c.getCustomizedPrivilege(y, 0);
        assertEquals(Wn.Io.modeFromOctal("0755"), pvg);

        pvg = c.getCustomizedPrivilege(z, 0);
        assertEquals(Wn.Io.modeFromOctal("0777"), pvg);

    }
}
