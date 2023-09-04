package org.nutz.walnut.util.obj;

import static org.junit.Assert.*;

import org.junit.Test;

public class WnObjJoinFieldsTest {

    @Test
    public void test() {
        String input = "event_id=id@~/events[evd_inner,evd_client:xyz]";
        WnObjJoinFields jfs = new WnObjJoinFields(input);
        assertEquals("event_id", jfs.getFromKey());
        assertEquals("id", jfs.getTargetKey());
        assertEquals("~/events", jfs.getTargetPath());
        assertTrue(jfs.hasFromKey());
        assertTrue(jfs.hasTargetKey());
        assertTrue(jfs.hasTargetPath());
        assertTrue(jfs.hasFields());
        assertEquals(2, jfs.getFields().length);
        assertEquals("evd_inner", jfs.getFields()[0].fromName);
        assertNull(jfs.getFields()[0].toName);
        assertEquals("evd_client", jfs.getFields()[1].fromName);
        assertEquals("xyz", jfs.getFields()[1].toName);
    }

}
