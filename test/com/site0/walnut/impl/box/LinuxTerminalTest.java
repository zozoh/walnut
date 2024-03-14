package com.site0.walnut.impl.box;

import static org.junit.Assert.*;

import org.junit.Test;

public class LinuxTerminalTest {

    @Test
    public void test_wrap_unwrap() {
        String s = "AA";
        String sw = LinuxTerminal.wrapFont(s, 1, 30);
        String su = LinuxTerminal.unwrapFont(sw);

        assertEquals(s, su);
    }

}
