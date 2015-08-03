package org.nutz.walnut.impl.io;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import org.junit.Test;
import org.nutz.lang.Lang;
import org.nutz.walnut.BaseStoreTest;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.io.WnBean;
import org.nutz.walnut.util.Wn;

public class WnStoreImplTest extends BaseStoreTest {

    private WnObj _o;

    @Test
    public void test_simple_read_write() throws IOException {
        WnObj o = new WnBean();

        // TODO 这里初始化 Obj 的字段

        String str = "hello";

        String hid = store.open(_o, Wn.S.RW);

        store.write(hid, str);

        String str2 = store.getString(hid);

        assertEquals(str, str2);
        assertEquals(str.length(), o.len());
        assertEquals(Lang.sha1(str), o.sha1());

    }

}
