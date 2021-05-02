package org.nutz.walnut.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class CmdsTest {

    @Test
    public void test_splitCmdArgs3() {
        String[] ss = Cmds.splitCmdArgs("a 'b \\'c \\`d\\` e\\''");
        String s = Ws.join(ss, ";");
        assertEquals("a;b 'c `d` e'", s);
    }

    @Test
    public void test_splitCmdArgs2() {
        String[] ss = Cmds.splitCmdArgs("a 'x\"y\\\"z op\"'");
        String s = Ws.join(ss, ";");
        assertEquals("a;x\"y\"z op\"", s);
    }

    @Test
    public void test_splitCmdArgs() {
        String[] ss = Cmds.splitCmdArgs("a b c");
        String s = Ws.join(ss, ";");
        assertEquals("a;b;c", s);

        ss = Cmds.splitCmdArgs("a'b'c");
        s = Ws.join(ss, ";");
        assertEquals("a;b;c", s);

        ss = Cmds.splitCmdArgs("a 'x y z' c");
        s = Ws.join(ss, ";");
        assertEquals("a;x y z;c", s);

        ss = Cmds.splitCmdArgs("a 'x \"y\" z' c");
        s = Ws.join(ss, ";");
        assertEquals("a;x \"y\" z;c", s);

        ss = Cmds.splitCmdArgs("a '''x \"y\" z''' c");
        s = Ws.join(ss, ";");
        assertEquals("a;x \"y\" z;c", s);

        ss = Cmds.splitCmdArgs("a '\\'x \"y\" z\\'' c");
        s = Ws.join(ss, ";");
        assertEquals("a;'x \"y\" z';c", s);
    }

    @Test
    public void test_splitCmdLines2() {
        String[] ss = Cmds.splitCmdLine("a 'b \\'c \\`d\\` e\\''");
        assertEquals(1, ss.length);
        assertEquals("a 'b \\'c \\`d\\` e\\''", ss[0]);
    }

    @Test
    public void test_splitCmdLine() {
        String[] lines = Cmds.splitCmdLine("A\\\nB\nC 'x\ny';D");
        assertEquals(3, lines.length);
        assertEquals("A B", lines[0]);
        assertEquals("C 'x\ny'", lines[1]);
        assertEquals("D", lines[2]);
    }

}
