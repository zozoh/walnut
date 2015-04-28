package org.nutz.walnut.api.io;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.junit.Test;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.walnut.WnStoreTest;
import org.nutz.walnut.impl.WnBean;

public abstract class AbstractWnStoreTest extends WnStoreTest {

    @Test
    public void test_simple_read_write() throws IOException {
        WnNode nd = tree.create(null, "abc.txt", WnRace.FILE);
        WnObj o = new WnBean().setNode(nd);
        String str = "hello";

        OutputStream ops = store.getOutputStream(o, 0);
        Streams.writeAndClose(new OutputStreamWriter(ops), str);

        InputStream ins = store.getInputStream(o, 0);
        String str2 = Streams.readAndClose(new InputStreamReader(ins));

        assertEquals(str, str2);

        o = indexer.get(nd.id());
        assertEquals(Lang.sha1(str), o.sha1());
        assertEquals(str.length(), o.len());
        assertEquals(o.lastModified(), o.nanoStamp() / 1000000L);
    }

}
