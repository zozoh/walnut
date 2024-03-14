package com.site0.walnut.alg.ds;

import static org.junit.Assert.*;

import org.junit.Test;
import com.site0.walnut.alg.exp.WnCharOpTable;

public class WnCharOpTableTest {

    @Test
    public void test_simple() {
        WnCharOpTable ot = new WnCharOpTable("+-", "*/", "()");

        assertEquals(0, ot.getPriority('+'));
        assertEquals(0, ot.getPriority('-'));

        assertEquals(1, ot.getPriority('*'));
        assertEquals(1, ot.getPriority('/'));

        assertEquals(2, ot.getPriority('('));
        assertEquals(2, ot.getPriority(')'));

        assertTrue(ot.compare('+', '-') == 0);
        assertTrue(ot.compare('+', '*') < 0);
        assertTrue(ot.compare('+', '(') < 0);
        assertTrue(ot.compare('*', '(') < 0);

        assertTrue(ot.compare('/', '-') > 0);
        assertTrue(ot.compare('(', '-') > 0);
        assertTrue(ot.compare('(', '*') > 0);
    }

}
