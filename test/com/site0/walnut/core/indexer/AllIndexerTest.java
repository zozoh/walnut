package com.site0.walnut.core.indexer;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import com.site0.walnut.core.indexer.localfile.LocalFileIndexerTest;
import com.site0.walnut.core.indexer.mongo.MongoIndexerTest;
import com.site0.walnut.core.indexer.mongo.MongosTest;
import com.site0.walnut.core.indexer.vofs.COSVofsIndexerTest;
import com.site0.walnut.core.indexer.vofs.S3VofsIndexerTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({LocalFileIndexerTest.class,
                     COSVofsIndexerTest.class,
                     S3VofsIndexerTest.class,
                     MongosTest.class,
                     MongoIndexerTest.class,})
public class AllIndexerTest {}
