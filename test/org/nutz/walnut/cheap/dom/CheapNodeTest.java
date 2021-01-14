package org.nutz.walnut.cheap.dom;

import static org.junit.Assert.*;

import org.junit.Test;

public class CheapNodeTest {

    @Test
    public void test_append_text_remove() {
        CheapElement $a = new CheapElement("a");
        CheapElement $b = new CheapElement("b");
        CheapElement $c = new CheapElement("c");
        CheapText $tx = new CheapText("X");
        CheapText $ty = new CheapText("Y");
        CheapText $tz = new CheapText("Z");

        $a.append($b);
        assertEquals("<a><b></b></a>", $a.toMarkup());

        $b.insertPrev($tx);
        assertEquals("<a>X<b></b></a>", $a.toMarkup());

        $b.insertNext($ty);
        assertEquals("<a>X<b></b>Y</a>", $a.toMarkup());

        $a.add(-1, $c);
        assertEquals("<a>X<b></b>Y<c></c></a>", $a.toMarkup());

        $a.add(-2, $tz);
        assertEquals("<a>X<b></b>YZ<c></c></a>", $a.toMarkup());
    }

    @Test
    public void test_add_remove() {
        CheapElement $a = new CheapElement("a");
        CheapElement $b = new CheapElement("b");
        CheapElement $c = new CheapElement("c");

        assertEquals("<a></a>", $a.toMarkup());

        $b.appendTo($a);
        assertEquals("<a><b></b></a>", $a.toMarkup());

        $c.appendTo($b);
        assertEquals("<a><b><c></c></b></a>", $a.toMarkup());

        $b.removeLastChild();
        assertEquals("<a><b></b></a>", $a.toMarkup());

        $a.removeLastChild();
        assertEquals("<a></a>", $a.toMarkup());
    }

}
