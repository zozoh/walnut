package org.nutz.walnut.core.indexer;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.nutz.walnut.core.indexer.localfile.LocalFileIndexerTest;
import org.nutz.walnut.core.indexer.mongo.MongoIndexerTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({LocalFileIndexerTest.class, MongoIndexerTest.class})
public class TestAllIndexer {}
