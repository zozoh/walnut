package com.site0.walnut.util.stream;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;
import org.nutz.lang.Streams;

import com.site0.walnut.util.Wlang;

public class MarkableInputStreamTest {

    @Test
    public void test_00() throws IOException {
        InputStream ins = MarkableInputStream.wrap(Wlang.ins("1234567890"));
        ins.mark(3);
        byte[] bs = new byte[3];
        int n = ins.read(bs);
        assertEquals(3, n);
        assertEquals('1', (int) bs[0]);
        assertEquals('2', (int) bs[1]);
        assertEquals('3', (int) bs[2]);
        ins.reset();

        bs = Streams.readBytes(ins);
        String s = new String(bs);
        assertEquals("1234567890", s);
    }

}
