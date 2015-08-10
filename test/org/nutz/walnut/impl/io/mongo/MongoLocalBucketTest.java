package org.nutz.walnut.impl.io.mongo;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;
import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.lang.Files;
import org.nutz.lang.random.R;
import org.nutz.mongo.ZMoCo;
import org.nutz.walnut.api.io.AbstractBucketTest;
import org.nutz.walnut.api.io.WnBucket;
import org.nutz.web.WebException;

public class MongoLocalBucketTest extends AbstractBucketTest {

    private MongoLocalBucketManager buckets;

    @Test
    public void test_duplicate_buckets() {
        // 首先给桶写点数据
        String str0 = R.sg(block_size * 3 + 2).next();
        String str1 = R.sg(block_size * 2).next();
        String str = str0 + str1;
        bu.write(str0);

        // 没有封盖的桶不能被复制
        try {
            bu.duplicateVirtual();
            fail();
        }
        catch (WebException e) {}

        // 那么封盖上
        bu.seal();

        // 复制一个
        WnBucket bu2 = bu.duplicateVirtual();
        assertEquals(str0, bu2.getString());
        assertEquals(bu.getId(), bu2.getParentBucketId());

        // 然后接着写
        bu2.append(str1);

        // 读桶，能读到完整的数据
        assertEquals(str, bu2.getString());

        // 封盖桶，可以看到桶还是关联了第一个桶
        bu2.seal();
        assertEquals(bu.getId(), bu2.getParentBucketId());

        // 再读桶，能读到完整的数据
        assertEquals(str, bu2.getString());

        // 解封，写写开头部分
        bu2.unseal();
        bu2.write(0, "___".getBytes(), 0, 3);

        // 封盖同，可以看到桶就不关联第一个桶了
        bu2.seal();
        assertNull(bu2.getParentBucketId());

        // 但是数据还是都能读到
        assertEquals("___" + str.substring(3), bu2.getString());
    }

    @Override
    protected void on_before(PropertiesProxy pp) {
        super.on_before(pp);
        block_size = 5;
        ZMoCo co = db.getCollection(pp.get("bucket-colnm"));
        File home = Files.createDirIfNoExists(pp.get("bucket-home"));
        buckets = new MongoLocalBucketManager(home, co);
        buckets._clean_for_unit_test();
        bu = buckets.alloc(block_size);
    }

    @Override
    protected void on_after(PropertiesProxy pp) {
        super.on_after(pp);
    }

}
