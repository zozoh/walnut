package com.site0.walnut.alg.ds.buf;

import static org.junit.Assert.*;

import java.util.List;
import java.util.ListIterator;

import org.junit.Test;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;

public class WnLinkedArrayListTest {

    private WnLinkedArrayList<String> _L(int width) {
        return new WnLinkedArrayList<>(String.class, width);
    }

    @Test
    public void test_first_last_popFirst() {
        WnLinkedArrayList<String> list = _L(3);
        list.addAll(Lang.list("A", "B", "C", "D"));

        assertEquals("A", list.first());
        assertEquals("D", list.last());
        assertEquals(4, list.size());
        assertEquals("ABCD", Strings.join("", list));

        list.popFirst();
        assertEquals("B", list.first());
        assertEquals("D", list.last());
        assertEquals(3, list.size());
        assertEquals("BCD", Strings.join("", list));

        list.popFirst();
        assertEquals("C", list.first());
        assertEquals("D", list.last());
        assertEquals(2, list.size());
        assertEquals("CD", Strings.join("", list));

        list.popFirst();
        assertEquals("D", list.first());
        assertEquals("D", list.last());
        assertEquals(1, list.size());
        assertEquals("D", Strings.join("", list));

        list.popFirst();
        assertNull(list.first());
        assertNull(list.last());
        assertEquals(0, list.size());
        assertEquals("", Strings.join("", list));

        assertNull(list.popFirst());
        assertNull(list.popFirst());
    }

    @Test
    public void test_first_last_popLast() {
        WnLinkedArrayList<String> list = _L(3);
        list.addAll(Lang.list("A", "B", "C", "D"));

        assertEquals("A", list.first());
        assertEquals("D", list.last());
        assertEquals(4, list.size());
        assertEquals("ABCD", Strings.join("", list));

        list.popLast();
        assertEquals("A", list.first());
        assertEquals("C", list.last());
        assertEquals(3, list.size());
        assertEquals("ABC", Strings.join("", list));

        list.popLast();
        assertEquals("A", list.first());
        assertEquals("B", list.last());
        assertEquals(2, list.size());
        assertEquals("AB", Strings.join("", list));

        list.popLast();
        assertEquals("A", list.first());
        assertEquals("A", list.last());
        assertEquals(1, list.size());
        assertEquals("A", Strings.join("", list));

        list.popLast();
        assertNull(list.first());
        assertNull(list.last());
        assertEquals(0, list.size());
        assertEquals("", Strings.join("", list));

        assertNull(list.popLast());
        assertNull(list.popLast());
    }

    @Test
    public void test_iterator() {
        List<String> list = _L(3);
        list.addAll(Lang.list("A", "B", "C", "D"));
        ListIterator<String> it = list.listIterator();

        assertTrue(it.hasNext());
        assertTrue(it.hasNext());
        assertEquals(0, it.nextIndex());
        assertEquals(0, it.nextIndex());
        assertEquals(-1, it.previousIndex());
        assertEquals(-1, it.previousIndex());
        assertFalse(it.hasPrevious());
        assertFalse(it.hasPrevious());

        assertEquals("A", it.next());
        assertTrue(it.hasPrevious());
        assertTrue(it.hasNext());
        assertEquals(0, it.previousIndex());
        assertEquals(1, it.nextIndex());

        assertEquals("B", it.next());
        assertTrue(it.hasPrevious());
        assertTrue(it.hasNext());
        assertEquals(1, it.previousIndex());
        assertEquals(2, it.nextIndex());

        assertEquals("C", it.next());
        assertTrue(it.hasPrevious());
        assertTrue(it.hasNext());
        assertEquals(2, it.previousIndex());
        assertEquals(3, it.nextIndex());

        assertEquals("D", it.next());
        assertTrue(it.hasPrevious());
        assertFalse(it.hasNext());
        assertEquals(3, it.previousIndex());
        assertEquals(4, it.nextIndex());

        assertEquals("D", it.previous());
        assertTrue(it.hasPrevious());
        assertTrue(it.hasNext());
        assertEquals(2, it.previousIndex());
        assertEquals(3, it.nextIndex());

        assertEquals("C", it.previous());
        assertTrue(it.hasPrevious());
        assertTrue(it.hasNext());
        assertEquals(1, it.previousIndex());
        assertEquals(2, it.nextIndex());

        assertEquals("B", it.previous());
        assertTrue(it.hasPrevious());
        assertTrue(it.hasNext());
        assertEquals(0, it.previousIndex());
        assertEquals(1, it.nextIndex());

        assertEquals("A", it.previous());
        assertFalse(it.hasPrevious());
        assertTrue(it.hasNext());
        assertEquals(-1, it.previousIndex());
        assertEquals(0, it.nextIndex());
    }

    @Test
    public void test_contains() {
        List<String> list = _L(3);
        list.addAll(Lang.list("A", "B", "C", "D"));

        assertTrue(list.contains("A"));
        assertTrue(list.contains("B"));
        assertTrue(list.contains("C"));
        assertTrue(list.contains("D"));
        assertFalse(list.contains("X"));
        assertFalse(list.contains("Y"));

        List<String> cans = Lang.list("A", "C", "D");
        assertTrue(list.containsAll(cans));

        cans = Lang.list("A", "C", "M");
        assertFalse(list.containsAll(cans));
    }

    @Test
    public void test_add_collections() {
        List<String> list = _L(3);
        list.addAll(Lang.list("A", "B", "C", "D"));
        assertEquals(4, list.size());
        list.addAll(2, Lang.list("x", "y", "z"));
        assertEquals(7, list.size());

        String[] ss = list.toArray(new String[list.size()]);
        assertEquals(7, ss.length);
        assertEquals("A", ss[0]);
        assertEquals("B", ss[1]);
        assertEquals("x", ss[2]);
        assertEquals("y", ss[3]);
        assertEquals("z", ss[4]);
        assertEquals("C", ss[5]);
        assertEquals("D", ss[6]);
    }

    @Test
    public void test_add_remove() {
        int width = 3;
        List<String> list = _L(width);
        list.add("A");
        assertEquals(1, list.size());
        list.add("B");
        assertEquals(2, list.size());
        list.add("C");
        assertEquals(3, list.size());
        list.add("D");
        assertEquals(4, list.size());

        assertEquals("A", list.get(0));
        assertEquals("B", list.get(1));
        assertEquals("C", list.get(2));
        assertEquals("D", list.get(3));

        String[] ss = list.toArray(new String[list.size()]);
        assertEquals(4, ss.length);
        assertEquals("A", ss[0]);
        assertEquals("B", ss[1]);
        assertEquals("C", ss[2]);
        assertEquals("D", ss[3]);

        list.remove(0);
        assertEquals(3, list.size());
        assertEquals("B", list.get(0));
        assertEquals("C", list.get(1));
        assertEquals("D", list.get(2));

        ss = list.toArray(new String[list.size()]);
        assertEquals(3, ss.length);
        assertEquals("B", ss[0]);
        assertEquals("C", ss[1]);
        assertEquals("D", ss[2]);

        list.remove(1);
        assertEquals(2, list.size());
        assertEquals("B", list.get(0));
        assertEquals("D", list.get(1));

        ss = list.toArray(new String[list.size()]);
        assertEquals(2, ss.length);
        assertEquals("B", ss[0]);
        assertEquals("D", ss[1]);

        list.remove(1);
        assertEquals(1, list.size());
        assertEquals("B", list.get(0));

        ss = list.toArray(new String[list.size()]);
        assertEquals(1, ss.length);
        assertEquals("B", ss[0]);

        list.remove(0);
        assertEquals(0, list.size());

        ss = list.toArray(new String[list.size()]);
        assertEquals(0, ss.length);
    }

}
