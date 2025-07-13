package com.site0.walnut.core.indexer;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import com.site0.walnut.core.indexer.localfile.LocalFileIndexerTest;
import com.site0.walnut.core.indexer.mongo.MongoIndexerTest;
import com.site0.walnut.core.indexer.mongo.MongosTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({LocalFileIndexerTest.class, MongosTest.class, MongoIndexerTest.class,})
public class AllIndexerTest {}
