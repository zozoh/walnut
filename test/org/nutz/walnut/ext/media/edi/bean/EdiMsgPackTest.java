package org.nutz.walnut.ext.media.edi.bean;

import static org.junit.Assert.*;

import org.junit.Test;
import org.nutz.lang.Files;

public class EdiMsgPackTest {

    @Test
    public void test_00() {
        String txt = Files.read("org/nutz/walnut/ext/media/edi/bean/pack/c00.edi.txt");
        EdiMsgPack pack = EdiMsgPack.parse(txt);
        String s2 = pack.toString();
        assertEquals(txt, s2);
    }

}
