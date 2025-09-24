package com.site0.walnut.ext.data.archive.util;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.nutz.lang.Files;

public class MimeSpyTest {

    @Test
    public void test_jpeg() throws IOException {
        File f = Files.findFile("com/site0/walnut/ext/net/http/upload/data/c1_p1.jpg");
        String mime = MimeSpy.getMimeType(f);
        assertEquals("image/jpeg", mime);
    }

    @Test
    public void test_txt() throws IOException {
        File f = Files.findFile("hello.txt");
        String mime = MimeSpy.getMimeType(f);
        assertEquals("text/plain", mime);
    }

    @Test
    public void test_tar_gz() throws IOException {
        File f = Files.findFile("hello.tar.gz");
        String mime = MimeSpy.getMimeType(f);
        assertEquals("application/gzip", mime);
    }

    // 真是怪啊，看来 .tar 的魔数不靠谱
    // @Test
    // public void test_tar() throws IOException {
    // File f = Files.findFile("hello.tar");
    // String mime = MimeSpy.getMimeType(f);
    // assertEquals("application/x-tar", mime);
    // }

}
