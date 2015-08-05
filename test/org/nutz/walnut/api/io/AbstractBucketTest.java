package org.nutz.walnut.api.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.nutz.lang.Lang;
import org.nutz.lang.random.R;
import org.nutz.walnut.BaseApiTest;

public abstract class AbstractBucketTest extends BaseApiTest {

    protected WnBucket bu;

    protected int block_size;

    @Test
    public void test_simple_read_write() {
        String str = R.sg(block_size * 3 + 2).next();

        bu.write(str);

        assertEquals(4, bu.getBlockNumber());
        assertEquals(str.length(), bu.getSize());

        long rc = bu.getCountRead();

        assertEquals(str, bu.getString());
        assertTrue(bu.getCountRead() > rc);

        WnBucketBlockInfo bi = new WnBucketBlockInfo();
        byte[] bs = new byte[block_size];

        int index = 0;
        assertEquals(block_size, bu.read(index, bs, bi));
        assertEquals(str.substring(index * block_size, (index + 1) * block_size), new String(bs));
        assertEquals(0, bi.paddingLeft);
        assertEquals(block_size, bi.size);
        assertEquals(0, bi.paddingRight);

        index = 1;
        assertEquals(block_size, bu.read(index, bs, bi));
        assertEquals(str.substring(index * block_size, (index + 1) * block_size), new String(bs));
        assertEquals(0, bi.paddingLeft);
        assertEquals(block_size, bi.size);
        assertEquals(0, bi.paddingRight);

        index = 2;
        assertEquals(block_size, bu.read(index, bs, bi));
        assertEquals(str.substring(index * block_size, (index + 1) * block_size), new String(bs));
        assertEquals(0, bi.paddingLeft);
        assertEquals(block_size, bi.size);
        assertEquals(0, bi.paddingRight);

        index = 3;
        assertEquals(2, bu.read(index, bs, bi));
        assertEquals(str.substring(index * block_size), new String(bs, 0, 2));
        assertEquals(0, bi.paddingLeft);
        assertEquals(2, bi.size);
        assertEquals(block_size - 2, bi.paddingRight);

        assertEquals(Lang.sha1(str), bu.getSha1());

        assertEquals(block_size, bu.read(2, bs, 0, block_size));
        assertEquals(str.substring(2, 2 + block_size), new String(bs));

    }

}
