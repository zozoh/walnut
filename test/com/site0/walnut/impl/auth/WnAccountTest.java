package com.site0.walnut.impl.auth;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.nutz.lang.random.R;

import com.site0.walnut.login.usr.WnUser;
import com.site0.walnut.login.usr.WnSimpleUser;

public class WnAccountTest {

    @Test
    public void test_getHomePath() {
        WnUser u = new WnSimpleUser("zozohtnt@gmail.com");
        u.setMainGroup("zozoh");
        u.setId(R.UU32());
        String expHomePath = "/home/zozoh";
        assertEquals(expHomePath, u.getHomePath());

        u.setName("xiaobai");
        expHomePath = "/home/xiaobai/";
        assertEquals(expHomePath, u.getHomePath());

        u.setHomePath("/a/b/");
        assertEquals("/a/b/", u.getHomePath());
    }

}
