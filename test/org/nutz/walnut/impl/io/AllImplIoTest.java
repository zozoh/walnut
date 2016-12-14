package org.nutz.walnut.impl.io;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.nutz.walnut.impl.io.bucket.MemoryBucketTest;
import org.nutz.walnut.impl.io.mnt.WnMemoryTree;
import org.nutz.walnut.impl.io.mongo.MongoLocalBucketTest;
import org.nutz.walnut.impl.io.mongo.WnMongosTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({MemoryBucketTest.class,
                     MongoLocalBucketTest.class,
                     WnMongosTest.class,
                     WnIoImplTest.class,
                     WnIoLocalMountTest.class,
                     WnMemoryTree.class})
public class AllImplIoTest {}
