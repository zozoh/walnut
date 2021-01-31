package org.nutz.walnut.core.indexer;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.nutz.walnut.core.indexer.dao.DaoIndexerTest;
import org.nutz.walnut.core.indexer.dao.DaoNoNameIndexerTest;
import org.nutz.walnut.core.indexer.localfile.LocalFileIndexerTest;
import org.nutz.walnut.core.indexer.mongo.MongoIndexerTest;
import org.nutz.walnut.core.indexer.mongo.MongosTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({LocalFileIndexerTest.class,
                     MongosTest.class,
                     MongoIndexerTest.class,
                     DaoIndexerTest.class,
                     DaoNoNameIndexerTest.class})
public class AllIndexerTest {}
