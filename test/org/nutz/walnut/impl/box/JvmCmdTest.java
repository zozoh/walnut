package org.nutz.walnut.impl.box;

import static org.junit.Assert.*;

import org.junit.Test;

public class JvmCmdTest {

    @Test
    public void test_parse_with_escape() {
        JvmCmd jc = new JvmCmd("abc '\\\"A\\\"'");
        assertEquals("abc", jc.cmdName);
        assertEquals(1, jc.args.length);
        assertEquals("\"A\"", jc.args[0]);

        jc = new JvmCmd("obj xyz -u 'icon:\"<i class=\\\"fa fa-tags></i>\"'");
        assertEquals("obj", jc.cmdName);
        assertEquals(3, jc.args.length);
        assertEquals("xyz", jc.args[0]);
        assertEquals("-u", jc.args[1]);
        assertEquals("icon:\"<i class=\"fa fa-tags></i>\"", jc.args[2]);

        jc = new JvmCmd("A B\\ C");
        assertEquals("A", jc.cmdName);
        assertEquals(1, jc.args.length);
        assertEquals("B C", jc.args[0]);
    }

    @Test
    public void test_parse_simple() {
        JvmCmd jc = new JvmCmd("    ls ");
        assertEquals("ls", jc.cmdName);
        assertEquals(0, jc.args.length);
        assertEquals(false, jc.redirectAppend);
        assertNull(jc.redirectPath);

        jc = new JvmCmd("    ls \t -l   'ab'c");
        assertEquals("ls", jc.cmdName);
        assertEquals(3, jc.args.length);
        assertEquals("-l", jc.args[0]);
        assertEquals("ab", jc.args[1]);
        assertEquals("c", jc.args[2]);
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
