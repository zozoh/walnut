package org.nutz.walnut.api.io;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Date;

import org.junit.Test;
import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.mongo.ZMoCo;
import org.nutz.walnut.impl.WnBean;
import org.nutz.walnut.impl.WnStoreFactoryImpl;
import org.nutz.walnut.impl.WnTreeFactoryImpl;
import org.nutz.walnut.impl.mongo.MongoWnIndexer;

public abstract class AbstractWnStoreTest extends AbstractWnApiTest {

    @Test
    public void test_simple_read_write() throws IOException {
        WnNode nd = tree.create(null, "abc.txt", WnRace.FILE);
        WnObj o = new WnBean().nd(nd);
        String str = "hello";

        OutputStream ops = store.getOutputStream(o, 0);
        Streams.writeAndClose(new OutputStreamWriter(ops), str);

        InputStream ins = store.getInputStream(o, 0);
        String str2 = Streams.readAndClose(new InputStreamReader(ins));

        assertEquals(str, str2);

        String sha1 = indexer.getString(nd.id(), "sha1");
        assertEquals(sha1, Lang.sha1(str));

        int len = indexer.getInt(nd.id(), "len");
        assertEquals(len, str.length());

        long nano = indexer.getLong(nd.id(), "nano");
        Date d = indexer.getTime(nd.id(), "lm");
        assertEquals(d.getTime(), nano / 1000000L);
    }

    private WnTree tree;

    private WnStore store;

    protected void on_before(PropertiesProxy pp) {
        treeFactory = new WnTreeFactoryImpl(db);

        tree = treeFactory.check("", getTreeMount());
        tree._clean_for_unit_test();

        ZMoCo co = db.getCollectionByMount("mongo:obj");
        indexer = new MongoWnIndexer(co);
        indexer._clean_for_unit_test();

        storeFactory = new WnStoreFactoryImpl(indexer, db, pp.check("local-sha1"));
        store = storeFactory.get(tree.getTreeNode());
        store._clean_for_unit_test();
    }

    protected abstract String getTreeMount();

}
