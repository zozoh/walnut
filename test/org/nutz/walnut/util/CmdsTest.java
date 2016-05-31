package org.nutz.walnut.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class CmdsTest {

    @Test
    public void test_split() {
        String[] lines = Cmds.splitCmdLine("A\\\nB\nC 'x\ny';D");
        assertEquals(3,lines.length);
        assertEquals("AB",lines[0]);
        assertEquals("C 'x\ny'",lines[1]);
        assertEquals("D",lines[2]);
    }

}
