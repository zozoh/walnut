package org.nutz.walnut.impl.io;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.junit.Test;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.walnut.BaseStoreTest;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.io.WnBean;

public class WnStoreImplTest extends BaseStoreTest {

    @Test
    public void test_simple_read_write() throws IOException {
        WnObj o = new WnBean();

        // TODO 这里初始化 Obj 的字段

        String str = "hello";

        OutputStream ops = store.getOutputStream(o, 0);
        Streams.writeAndClose(new OutputStreamWriter(ops), str);

        InputStream ins = store.getInputStream(o, 0);
        String str2 = Streams.readAndClose(new InputStreamReader(ins));

        assertEquals(str, str2);
        assertEquals(str.length(), o.len());
        assertEquals(Lang.sha1(str), o.sha1());

    }

}
