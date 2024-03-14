package com.site0.walnut.cheap.dom.match;

import static org.junit.Assert.*;

import org.junit.Test;

public class CheapAutoMatcherTest {

    @Test
    public void test_00() {
        String str, s0, s2;
        CheapAutoMatcher cm;

        str = "a.link[href='#']";
        cm = new CheapAutoMatcher(str);
        s2 = cm.toString();
        s0 = str;
        assertEquals(str, s2);

        str = "a.link[href='#'][test=true][abc]";
        cm = new CheapAutoMatcher(str);
        s2 = cm.toString();
        s0 = "a.link[href='#'][test='true'][abc]";
        assertEquals(s0, s2);
    }

}
