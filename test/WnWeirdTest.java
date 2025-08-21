import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.site0.walnut.core.indexer.AllIndexerTest;
import com.site0.walnut.core.indexer.localfile.LocalFileIndexerTest;
import com.site0.walnut.core.indexer.mongo.MongoIndexerTest;
import com.site0.walnut.core.indexer.mongo.MongosTest;
import com.site0.walnut.core.indexer.vofs.WnVofsIndexerTest;
import com.site0.walnut.util.AllUtilTest;
import com.site0.walnut.util.WnTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({WnVofsIndexerTest.class,
                     MongoIndexerTest.class,
                     WnTest.class})
public class WnWeirdTest {}
