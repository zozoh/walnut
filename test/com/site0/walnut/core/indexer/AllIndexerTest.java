package com.site0.walnut.core.indexer;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import com.site0.walnut.core.indexer.localfile.LocalFileIndexerTest;
import com.site0.walnut.core.indexer.mongo.MongoIndexerTest;
import com.site0.walnut.core.indexer.mongo.MongosTest;
import com.site0.walnut.core.indexer.vofs.WnVofsIndexerTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({LocalFileIndexerTest.class,
                     WnVofsIndexerTest.class,
                     MongosTest.class,
                     MongoIndexerTest.class,})
public class AllIndexerTest {}
