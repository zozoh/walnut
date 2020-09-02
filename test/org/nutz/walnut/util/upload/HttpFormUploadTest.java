package org.nutz.walnut.util.upload;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;
import org.nutz.json.Json;
import org.nutz.lang.Files;
import org.nutz.lang.Streams;
import org.nutz.lang.util.NutMap;

public class HttpFormUploadTest {

    private String getMyDataPath() {
        String myPh = this.getClass().getName().replace('.', '/');
        return Files.getParent(myPh);
    }

    private String getMyDataFile(String name) {
        String paPh = getMyDataPath();
        return paPh + "/data/" + name;
    }

    private NutMap doUpload(String nm) throws IOException {
        String phHead = getMyDataFile(nm + "_head.json");
        String phBody = getMyDataFile(nm + "_body.txt");

        File fHead = Files.findFile(phHead);
        File fBody = Files.findFile(phBody);

        NutMap head = Json.fromJsonFile(NutMap.class, fHead);
        InputStream ins = Streams.fileIn(fBody);

        String bound = head.getString("http-header-CONTENT-TYPE");
        HttpFormUpload upload = new HttpFormUpload(ins, bound, 240);

        return upload.parseDataAndClose();
    }

    @Test
    public void test_1() throws IOException {
        NutMap map = doUpload("c1");

        assertEquals(3, map.size());
        assertEquals("hello", map.get("test"));

        byte[] be, bs;

        HttpFormFile p0 = map.getAs("p0", HttpFormFile.class);
        assertEquals("c1_p0.png", p0.getName());
        assertEquals("image/png", p0.getContentType());
        bs = p0.getBytes();
        File f0 = Files.findFile(getMyDataFile("c1_p0.png"));
        be = Files.readBytes(f0);
        assertTrue(bs.length > 0);
        assertEquals(be.length, bs.length);
        for (int i = 0; i < bs.length; i++) {
            assertEquals(be[i], bs[i]);
        }

        HttpFormFile p1 = map.getAs("p1", HttpFormFile.class);
        assertEquals("c1_p1.jpg", p1.getName());
        assertEquals("image/jpeg", p1.getContentType());
        bs = p0.getBytes();
        File f1 = Files.findFile(getMyDataFile("c1_p0.png"));
        be = Files.readBytes(f1);
        assertTrue(bs.length > 0);
        assertEquals(be.length, bs.length);
        for (int i = 0; i < bs.length; i++) {
            assertEquals(be[i], bs[i]);
        }

    }

    @Test
    public void test_0() throws IOException {
        NutMap map = doUpload("c0");

        assertEquals(4, map.size());
        assertEquals("test_upload", map.get("name"));
        assertEquals("13", map.get("age"));

        HttpFormFile f1 = map.getAs("f1", HttpFormFile.class);
        assertEquals("hello.txt", f1.getName());
        assertEquals("text/plain", f1.getContentType());
        assertEquals("[hello]", f1.getContent());

        HttpFormFile f2 = map.getAs("f2", HttpFormFile.class);
        assertEquals("world.txt", f2.getName());
        assertEquals("text/plain", f2.getContentType());
        assertEquals("[world]", f2.getContent());
    }

}
