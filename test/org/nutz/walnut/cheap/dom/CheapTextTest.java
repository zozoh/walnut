package org.nutz.walnut.cheap.dom;

import static org.junit.Assert.*;

import org.junit.Test;

public class CheapTextTest {

    @Test
    public void test_entities() {
        CheapText t = new CheapText("A&nbsp;B", true);
        assertEquals("A B", t.decodeText());

        t = new CheapText("&hellip;&hellip;", true);
        assertEquals("……", t.decodeText());
        
        t = new CheapText("：&ldquo;X", true);
        assertEquals("：“X", t.decodeText());
    }

}
