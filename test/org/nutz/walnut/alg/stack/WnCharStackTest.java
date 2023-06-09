package org.nutz.walnut.alg.stack;

import static org.junit.Assert.*;

import org.junit.Test;

public class WnCharStackTest {

    @Test
    public void test_simple() {
        WnCharStack cs = new WnCharStack('{', '}');
        String re = cs.process("A {{x:100}} B");
        assertEquals("{x:100}", re);
    }

    @Test
    public void test_toArray() {
        WnCharStack cs = new WnCharStack('{', '}');
        String[] ss = cs.processAsArray("{{x:100}} {<abc>} {x:{n:t,b:{q:0}}}");
        assertEquals(3, ss.length);
        assertEquals("{x:100}", ss[0]);
        assertEquals("<abc>", ss[1]);
        assertEquals("x:{n:t,b:{q:0}}", ss[2]);
    }

    @Test
    public void test_quote() {
        WnCharStack cs = new WnCharStack("\"'");
        String re = cs.process("\"abc\"");
        assertEquals("abc", re);

        re = cs.process("\"ab\\nc\"");
        assertEquals("ab\nc", re);

        re = cs.process("'\"ab\\nc\"'");
        assertEquals("\"ab\nc\"", re);
    }

    @Test
    public void test_quote2() {
        WnCharStack cs = new WnCharStack("\"'");
        String re = cs.process("\"ab'x\\\"y'c\"");
        assertEquals("ab'x\"y'c", re);
    }

}
