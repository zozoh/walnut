package org.nutz.walnut.impl.io.local;

import static org.junit.Assert.*;

import org.junit.Test;
import org.nutz.lang.Strings;
import org.nutz.walnut.impl.io.local.MemNodeItem;
import org.nutz.walnut.impl.io.local.MemNodeMap;

public class MemNodeMapTest {

    @Test
    public void test_one_line_with_mount() {
        String str = "aaa:/a>>file:///path/to/file";

        MemNodeMap mnm = new MemNodeMap();
        mnm.fromString(str);

        MemNodeItem mni = mnm.getById("aaa");

        assertEquals("aaa", mni.id);
        assertEquals("/a", mni.path);
        assertEquals("file:///path/to/file", mni.mount);

        assertEquals(str, Strings.trim(mnm.toString()));
    }

}
