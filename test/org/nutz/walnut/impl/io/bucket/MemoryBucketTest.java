package org.nutz.walnut.impl.io.bucket;

import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.walnut.api.io.AbstractBucketTest;

public class MemoryBucketTest extends AbstractBucketTest {

    @Override
    protected void on_before(PropertiesProxy pp) {
        super.on_before(pp);
        block_size = 5;
        bu = new MemoryBucket(block_size);
    }

}
