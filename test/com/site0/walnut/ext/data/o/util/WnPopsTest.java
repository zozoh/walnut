package com.site0.walnut.ext.data.o.util;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Ws;

public class WnPopsTest {

    @Test
    public void test_pop_all_or_nil() {
        List<String> list;
        WnPop pop;
        String s;

        list = Wlang.list("A", "B", "C", "D", "E");
        pop = WnPops.parse("<nil>");
        list = pop.exec(list);
        s = Ws.join(list, "");
        assertEquals("ABCDE", s);

        list = Wlang.list("A", "B", "C", "D", "E");
        pop = WnPops.parse("<all>");
        list = pop.exec(list);
        s = Ws.join(list, "");
        assertEquals("", s);
    }

    @Test
    public void test_popRegex() {
        List<String> list;
        WnPop pop;
        String s;

        list = Wlang.list("A", "B", "C", "D", "E");
        pop = WnPops.parse("^[A-C]$");
        list = pop.exec(list);
        s = Ws.join(list, "");
        assertEquals("DE", s);

        list = Wlang.list("A", "B", "C", "D", "E");
        pop = WnPops.parse("!^[A-C]$");
        list = pop.exec(list);
        s = Ws.join(list, "");
        assertEquals("ABC", s);
    }

    @Test
    public void test_popEquals() {
        List<String> list;
        WnPop pop;
        String s;

        list = Wlang.list("A", "B", "C", "D", "E");
        pop = WnPops.parse("=A");
        list = pop.exec(list);
        s = Ws.join(list, "");
        assertEquals("BCDE", s);

        list = Wlang.list("A", "B", "C", "D", "E");
        pop = WnPops.parse("C");
        list = pop.exec(list);
        s = Ws.join(list, "");
        assertEquals("ABDE", s);

        list = Wlang.list("A", "B", "C", "D", "E");
        pop = WnPops.parse("!=D");
        list = pop.exec(list);
        s = Ws.join(list, "");
        assertEquals("D", s);
    }

    @Test
    public void test_popEnum() {
        List<String> list;
        WnPop pop;
        String s;

        list = Wlang.list("A", "B", "C", "D", "E");
        pop = WnPops.parse("[B,D]");
        list = pop.exec(list);
        s = Ws.join(list, "");
        assertEquals("ACE", s);

        list = Wlang.list("A", "B", "C", "D", "E");
        pop = WnPops.parse("[]");
        list = pop.exec(list);
        s = Ws.join(list, "");
        assertEquals("ABCDE", s);
    }

    @Test
    public void test_popI() {
        List<String> list;
        WnPop pop;
        String s;

        list = Wlang.list("A", "B", "C", "D", "E");
        pop = WnPops.parse("i3");
        list = pop.exec(list);
        s = Ws.join(list, "");
        assertEquals("ABCE", s);

        list = Wlang.list("A", "B", "C", "D", "E");
        pop = WnPops.parse("i10");
        list = pop.exec(list);
        s = Ws.join(list, "");
        assertEquals("ABCDE", s);

        list = Wlang.list("A", "B", "C", "D", "E");
        pop = WnPops.parse("i-3");
        list = pop.exec(list);
        s = Ws.join(list, "");
        assertEquals("ABDE", s);

        list = Wlang.list("A", "B", "C", "D", "E");
        pop = WnPops.parse("i-1");
        list = pop.exec(list);
        s = Ws.join(list, "");
        assertEquals("ABCD", s);
    }

    @Test
    public void test_popN() {
        List<String> list;
        WnPop pop;
        String s;

        list = Wlang.list("A", "B", "C", "D", "E");
        pop = WnPops.parse("3");
        list = pop.exec(list);
        s = Ws.join(list, "");
        assertEquals("AB", s);

        list = Wlang.list("A", "B", "C", "D", "E");
        pop = WnPops.parse("0");
        list = pop.exec(list);
        s = Ws.join(list, "");
        assertEquals("ABCDE", s);

        list = Wlang.list("A", "B", "C", "D", "E");
        pop = WnPops.parse("-3");
        list = pop.exec(list);
        s = Ws.join(list, "");
        assertEquals("DE", s);

        list = Wlang.list("A", "B", "C", "D", "E");
        pop = WnPops.parse("-1");
        list = pop.exec(list);
        s = Ws.join(list, "");
        assertEquals("BCDE", s);
    }

}
