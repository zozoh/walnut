package org.nutz.walnut.impl.io;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.nutz.walnut.impl.io.bucket.MemoryBucketTest;
import org.nutz.walnut.impl.io.mongo.MongoLocalBucketTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({WnStoreImplTest.class,
                     WnIoImplTest.class,
                     MemoryBucketTest.class,
                     MongoLocalBucketTest.class})
public class AllImplIoTest {}
