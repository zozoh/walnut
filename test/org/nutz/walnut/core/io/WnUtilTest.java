package org.nutz.walnut.core.io;

import static org.junit.Assert.*;

import org.junit.Test;
import org.nutz.walnut.util.Wn;

public class WnUtilTest {

    @Test
    public void test_s() {
        int mode = Wn.S.WM;
        assertEquals(false, Wn.S.canRead(mode));
        assertEquals(true, Wn.S.canWite(mode));
        assertEquals(false, Wn.S.canAppend(mode));
        assertEquals(true, Wn.S.canModify(mode));
        assertEquals(true, Wn.S.canWriteOrAppend(mode));
        assertEquals(false, Wn.S.isRead(mode));
        assertEquals(false, Wn.S.isWrite(mode));
        assertEquals(true, Wn.S.isWriteModify(mode));
        assertEquals(false, Wn.S.isWriteAppend(mode));
        assertEquals(false, Wn.S.isReadWrite(mode));

        mode = Wn.S.RW;
        assertEquals(true, Wn.S.canRead(mode));
        assertEquals(true, Wn.S.canWite(mode));
        assertEquals(false, Wn.S.canAppend(mode));
        assertEquals(false, Wn.S.canModify(mode));
        assertEquals(true, Wn.S.canWriteOrAppend(mode));
        assertEquals(false, Wn.S.isRead(mode));
        assertEquals(false, Wn.S.isWrite(mode));
        assertEquals(false, Wn.S.isWriteModify(mode));
        assertEquals(false, Wn.S.isWriteAppend(mode));
        assertEquals(true, Wn.S.isReadWrite(mode));
    }

}
