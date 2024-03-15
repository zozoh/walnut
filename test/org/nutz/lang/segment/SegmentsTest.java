package org.nutz.lang.segment;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.util.Context;

public class SegmentsTest {

    @Test
    public void test_simple_replace() {
        String ptn = "1${A}2${B}3${C}4";
        Context context = Wlang.context();
        context.set("B", "haha");
        String str = Segments.replace(ptn, context);

        assertEquals("1${A}2haha3${C}4", str);
    }
    
    @Test
    public void test_issue_722() {
        Context ctx = Wlang.context();
        assertEquals("^.+abc.+$", Segments.replace("^.+abc.+$", ctx));
//        assertEquals("^.+abc.+${", Segments.replace("^.+abc.+${", ctx));
    }

}
