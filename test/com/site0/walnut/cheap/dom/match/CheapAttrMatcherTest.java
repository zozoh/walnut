package com.site0.walnut.cheap.dom.match;

import static org.junit.Assert.*;

import org.junit.Test;

public class CheapAttrMatcherTest {

    @Test
    public void test_03() {
        String s2;
        CheapAttrMatcher am;

        am = new CheapAttrMatcher("[abc=\"oo\"]");
        s2 = am.toString();
        assertEquals("[abc='oo']", s2);
    }

    @Test
    public void test_02() {
        String s2;
        CheapAttrMatcher am;

        am = new CheapAttrMatcher("[abc='xyz']");
        s2 = am.toString();
        assertEquals("[abc='xyz']", s2);

    }

    @Test
    public void test_01() {
        String s2;
        CheapAttrMatcher am;

        am = new CheapAttrMatcher("[abc=true]");
        s2 = am.toString();
        assertEquals("[abc='true']", s2);

    }

    @Test
    public void test_00() {
        String s2;
        CheapAttrMatcher am;

        am = new CheapAttrMatcher("[abc]");
        s2 = am.toString();
        assertEquals("[abc]", s2);
    }

}
