package org.nutz.walnut.api.io.mongo;

import org.nutz.walnut.api.io.AbstractWnTreeTest;

public class MongoWnTreeTest extends AbstractWnTreeTest {

    @Override
    protected String my_key(String key) {
        return "mnt-mongo-" + key;
    }

    @Override
    protected String ta_key(String key) {
        return "mnt-local-" + key;
    }

}
