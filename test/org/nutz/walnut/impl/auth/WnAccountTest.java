package org.nutz.walnut.impl.auth;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.nutz.lang.random.R;
import org.nutz.walnut.api.auth.WnAccount;

public class WnAccountTest {

    @Test
    public void test_getHomePath() {
        WnAccount u = new WnAccount("zozohtnt@gmail.com");
        u.setId(R.UU32());
        String expHomePath = "/home/" + u.getId() + "/";
        assertEquals(expHomePath, u.getHomePath());

        u.setName("xiaobai");
        expHomePath = "/home/xiaobai/";
        assertEquals(expHomePath, u.getHomePath());

        u.setHomePath("/a/b/");
        assertEquals("/a/b/", u.getHomePath());
    }

}
