package org.nutz.walnut.api.io.local;

import org.nutz.walnut.api.io.AbstractWnTreeTest;

public class LocalWnTreeTest extends AbstractWnTreeTest {

    @Override
    protected String my_key(String key) {
        return "mnt-local-" + key;
    }
    
    @Override
    protected String ta_key(String key) {
        return "mnt-mongo-" + key;
    }

}
