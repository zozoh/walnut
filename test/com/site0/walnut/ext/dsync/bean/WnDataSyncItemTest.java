package com.site0.walnut.ext.dsync.bean;

import static org.junit.Assert.*;

import org.junit.Test;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.ext.sys.dsync.bean.WnDataSyncItem;

public class WnDataSyncItemTest {

    @Test
    public void test_01() {
        String s1 = "059c491efa79ef6938740854a8843390acfdffdc";
        String s2 = "2ce79dd97a6af1d42a40ff7f6fcebe657dcc3986";
        String input = "D:~/path/to/a;=BEAN(" + s1 + ");=META(" + s2 + ")";

        WnDataSyncItem dsi = new WnDataSyncItem(input);
        assertEquals(WnRace.DIR, dsi.getRace());
        assertEquals("~/path/to/a", dsi.getPath());
        assertEquals(s1, dsi.getBeanSha1());
        assertEquals(s2, dsi.getMetaSha1());
        assertNull(dsi.getSha1());

        String str = dsi.toString();
        assertEquals(input, str);
    }
    
    @Test
    public void test_nil_sha1() {
        String s1 = "059c491efa79ef6938740854a8843390acfdffdc";
        String s2 = "2ce79dd97a6af1d42a40ff7f6fcebe657dcc3986";
        String input = "F:~/path/to/a;=BEAN(" + s1 + ");=META(" + s2 + ");=SHA1(null);=LEN(0)";

        WnDataSyncItem dsi = new WnDataSyncItem(input);
        assertEquals(WnRace.FILE, dsi.getRace());
        assertEquals("~/path/to/a", dsi.getPath());
        assertEquals(s1, dsi.getBeanSha1());
        assertEquals(s2, dsi.getMetaSha1());
        assertNull(dsi.getSha1());

        String str = dsi.toString();
        assertEquals(input, str);
    }


    @Test
    public void test_00() {
        String s1 = "059c491efa79ef6938740854a8843390acfdffdc";
        String s2 = "2ce79dd97a6af1d42a40ff7f6fcebe657dcc3986";
        String s3 = "b45b66e9ce21b909e3264a88f674e3f89b01e3ac";
        String input = "F:~/path/to/a;=BEAN(" + s1 + ");=META(" + s2 + ");=SHA1(" + s3 + ");=LEN(894321)";

        WnDataSyncItem dsi = new WnDataSyncItem(input);
        assertEquals(WnRace.FILE, dsi.getRace());
        assertEquals("~/path/to/a", dsi.getPath());
        assertEquals(s1, dsi.getBeanSha1());
        assertEquals(s2, dsi.getMetaSha1());
        assertEquals(s3, dsi.getSha1());

        String str = dsi.toString();
        assertEquals(input, str);
    }

}
