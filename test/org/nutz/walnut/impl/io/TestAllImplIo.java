package org.nutz.walnut.impl.io;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.nutz.walnut.impl.io.bucket.TestMemoryBucket;
import org.nutz.walnut.impl.io.memory.TestWnMemoryTree;
import org.nutz.walnut.impl.io.mongo.TestMongoLocalBucket;
import org.nutz.walnut.impl.io.mongo.TestWnMongos;

@RunWith(Suite.class)
@Suite.SuiteClasses({TestMemoryBucket.class,
                     TestMongoLocalBucket.class,
                     TestWnMongos.class,
                     TestWnIoImpl.class,
                     TestWnIoLocalMount.class,
                     TestWnMemoryTree.class})
public class TestAllImplIo {}
