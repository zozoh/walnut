package org.nutz.walnut.impl.io.mongo;

import java.io.File;

import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.lang.Files;
import org.nutz.mongo.ZMoCo;
import org.nutz.walnut.api.io.AbstractBucketTest;

public class MongoLocalBucketTest extends AbstractBucketTest {

    private MongoLocalBucketManager buckets;

    @Override
    protected void on_before(PropertiesProxy pp) {
        super.on_before(pp);
        block_size = 5;
        ZMoCo co = db.getCollection("bucket-colnm");
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
