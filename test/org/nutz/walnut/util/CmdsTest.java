package org.nutz.walnut.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class CmdsTest {

    @Test
    public void test_splitCmdAtoms() {
        String[] ss = Cmds.splitCmdAtoms("a|b|c");
        String s = Ws.join(ss, ";");
        assertEquals("a;b;c", s);

        ss = Cmds.splitCmdAtoms("a'b|c'd");
        s = Ws.join(ss, ";");
        assertEquals("a'b|c'd", s);

        ss = Cmds.splitCmdAtoms("ab\\|c");
        s = Ws.join(ss, ";");
        assertEquals("ab|c", s);
    }

    @Test
    public void test_splitCmdArgs4() {
        String[] ss = Cmds.splitCmdArgs("a\r\nb");
        String s = Ws.join(ss, ";");
        assertEquals("a;b", s);
    }

    @Test
    public void test_splitCmdArgs3() {
        String[] ss = Cmds.splitCmdArgs("a 'b \\'c \\`d\\` e\\''");
        String s = Ws.join(ss, ";");
        assertEquals("a;'b 'c `d` e''", s);
    }

    @Test
    public void test_splitCmdArgs2() {
        String[] ss = Cmds.splitCmdArgs("a 'x\"y\\\"z op\"'");
        String s = Ws.join(ss, ";");
        assertEquals("a;'x\"y\"z op\"'", s);
    }

    @Test
    public void test_splitCmdArgs() {
        String[] ss = Cmds.splitCmdArgs("a b c");
        String s = Ws.join(ss, ";");
        assertEquals("a;b;c", s);

        ss = Cmds.splitCmdArgs("a'b'c");
        s = Ws.join(ss, ";");
        assertEquals("a;'b';c", s);

        ss = Cmds.splitCmdArgs("a 'x y z' c");
        s = Ws.join(ss, ";");
        assertEquals("a;'x y z';c", s);

        ss = Cmds.splitCmdArgs("a 'x \"y\" z' c");
        s = Ws.join(ss, ";");
        assertEquals("a;'x \"y\" z';c", s);

        ss = Cmds.splitCmdArgs("a '''x \"y\" z''' c");
        s = Ws.join(ss, ";");
        assertEquals("a;'x \"y\" z';c", s);

        ss = Cmds.splitCmdArgs("a '\\'x \"y\" z\\'' c");
        s = Ws.join(ss, ";");
        assertEquals("a;''x \"y\" z'';c", s);
    }

    @Test
    public void test_splitCmdLines3() {
        String[] ss = Cmds.splitCmdLines("a\r\nb");
        assertEquals(2, ss.length);
        assertEquals("a", ss[0]);
        assertEquals("b", ss[1]);
    }

    @Test
    public void test_splitCmdLines2() {
        String[] ss = Cmds.splitCmdLines("a 'b \\'c \\`d\\` e\\''");
        assertEquals(1, ss.length);
        assertEquals("a 'b \\'c \\`d\\` e\\''", ss[0]);
    }

    @Test
    public void test_splitCmdLine() {
        String[] lines = Cmds.splitCmdLines("A\\\nB\nC 'x\ny';D");
        assertEquals(3, lines.length);
        assertEquals("A B", lines[0]);
        assertEquals("C 'x\ny'", lines[1]);
        assertEquals("D", lines[2]);
    }

}
