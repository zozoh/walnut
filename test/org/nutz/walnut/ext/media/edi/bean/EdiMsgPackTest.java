package org.nutz.walnut.ext.media.edi.bean;

import static org.junit.Assert.*;

import org.junit.Test;
import org.nutz.lang.Files;
import org.nutz.walnut.util.Wlang;
import org.nutz.walnut.util.tmpl.WnTmplX;

public class EdiMsgPackTest {

    private String _read_input(String name) {
        String txt = Files.read("org/nutz/walnut/ext/media/edi/bean/pack/" + name + ".edi.txt");
        return txt.replaceAll("\r\n", "\n").trim();
    }

    @Test
    public void test_00() {
        String input = _read_input("c00");
        EdiMsgPack pack = EdiMsgPack.parse(input);
        String s2 = pack.toString().trim();
        assertEquals(input, s2);

        String in2 = WnTmplX.exec(input, Wlang.map("N", 3));
        pack.packEntry();
        String s3 = pack.toString().trim();
        assertEquals(in2, s3);
    }

    @Test
    public void test_01() {
        String input = _read_input("c01");
        EdiMsgPack pack = EdiMsgPack.parse(input);
        String s2 = pack.toString().trim();
        assertEquals(input, s2);

        String in2 = WnTmplX.exec(input, Wlang.map("N", 10));
        pack.packEntry();
        String s3 = pack.toString().trim();
        assertEquals(in2, s3);
    }
    
    @Test
    public void test_02() {
        String input = _read_input("c02");
        EdiMsgPack pack = EdiMsgPack.parse(input);
        String s2 = pack.toString().trim();
        assertEquals(input, s2);

        String in2 = WnTmplX.exec(input, Wlang.map("N", 4));
        pack.packEntry();
        String s3 = pack.toString().trim();
        assertEquals(in2, s3);
    }
    
    @Test
    public void test_03() {
        String input = _read_input("c03");
        EdiMsgPack pack = EdiMsgPack.parse(input);
        String s2 = pack.toString().trim();
        assertEquals(input, s2);

        String in2 = WnTmplX.exec(input, Wlang.map("N", 57));
        pack.packEntry();
        String s3 = pack.toString().trim();
        assertEquals(in2, s3);
    }

}
