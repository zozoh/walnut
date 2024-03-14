package com.site0.walnut.core.eot.mongo;

import org.junit.Before;
import org.nutz.mongo.ZMoCo;
import com.site0.walnut.core.eot.AbstractExpiObjTableTest;

public class MongoExpiObjTableTest extends AbstractExpiObjTableTest {

    @Before
    public void setUp() throws Exception {
        ZMoCo co = this.setup.getMongoCoExpi();
        this.table = new MongoExpiObjTable(co);
    }

}
