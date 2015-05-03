package org.nutz.walnut.impl.box;

import static org.junit.Assert.*;

import org.junit.Test;

public class JvmCmdTest {

    @Test
    public void test_parse_simple() {
        JvmCmd jc = new JvmCmd("    ls ");
        assertEquals("ls", jc.cmdName);
        assertEquals(0, jc.args.length);
        assertEquals(false, jc.redirectAppend);
        assertNull(jc.redirectPath);

        jc = new JvmCmd("    ls \t -l   'ab'c");
        assertEquals("ls", jc.cmdName);
        assertEquals(2, jc.args.length);
        assertEquals("-l", jc.args[0]);
        assertEquals("abc", jc.args[1]);
        assertEquals(false, jc.redirectAppend);
        assertNull(jc.redirectPath);

        jc = new JvmCmd("echo 'haha' > ~/abc.txt");
        assertEquals("echo", jc.cmdName);
        assertEquals(1, jc.args.length);
        assertEquals("haha", jc.args[0]);
        assertEquals(false, jc.redirectAppend);
        assertEquals("~/abc.txt", jc.redirectPath);

        jc = new JvmCmd("echo 'haha' >> ~/abc.txt");
        assertEquals("echo", jc.cmdName);
        assertEquals(1, jc.args.length);
        assertEquals("haha", jc.args[0]);
        assertEquals(true, jc.redirectAppend);
        assertEquals("~/abc.txt", jc.redirectPath);
    }

}
