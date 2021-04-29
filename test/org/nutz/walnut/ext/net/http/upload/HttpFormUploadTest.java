package org.nutz.walnut.ext.net.http.upload;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.Test;
import org.nutz.json.Json;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.lang.util.LinkedByteBuffer;
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
        HttpFormUpload upload = new HttpFormUpload(ins, bound, 300);

        return upload.parseDataAndClose();
    }

    @Test
    public void test_3_1024_buf_size() throws IOException {
        String phHead = getMyDataFile("c3_head.json");
        String phBody = getMyDataFile("c3_body.txt");

        File fHead = Files.findFile(phHead);
        File fBody = Files.findFile(phBody);
        System.out.println(Lang.sha1(fBody));

        NutMap head = Json.fromJsonFile(NutMap.class, fHead);
        InputStream ins = Streams.fileIn(fBody);

        String bound = head.getString("http-header-CONTENT-TYPE");
        HttpFormUpload upload = new HttpFormUpload(ins, bound);

        LinkedByteBuffer opbuf = new LinkedByteBuffer();
        HttpFormFile fld0 = new HttpFormFile();

        upload.parse(new HttpFormCallback() {
            public void handle(HttpFormUploadField field) throws IOException {
                if (!field.isFile() || !field.isName("file")) {
                    throw Lang.makeThrow("Invalid field %s", field.getName());
                }
                // 记录
                fld0.setName(field.getFileName());
                fld0.setContentType(field.getContentType());

                // 读内容
                byte[] bs = field.readAllBytes();
                opbuf.write(bs);
            }
        });

        byte[] bs = opbuf.toArray();

        assertEquals("Fb0oSlQIkDLwa99933f6e5b2af5b105ee6c696627fc4.jpg", fld0.getName());
        assertEquals("image/jpeg", fld0.getContentType());

        File f0 = Files.findFile(getMyDataFile("c3.jpg"));
        byte[] be = Files.readBytes(f0);
        assertTrue(bs.length > 0);
        assertEquals(be.length, bs.length);
        for (int i = 0; i < bs.length; i++) {
            assertEquals(be[i], bs[i]);
        }
    }

    @Test
    public void test_3() throws IOException {
        NutMap map = doUpload("c3");

        assertEquals(1, map.size());

        byte[] be, bs;

        List<HttpFormFile> files = map.getList("file", HttpFormFile.class);

        assertEquals(1, files.size());

        HttpFormFile fld0 = files.get(0);
        assertEquals("Fb0oSlQIkDLwa99933f6e5b2af5b105ee6c696627fc4.jpg", fld0.getName());
        assertEquals("image/jpeg", fld0.getContentType());
        bs = fld0.getBytes();
        File f0 = Files.findFile(getMyDataFile("c3.jpg"));
        be = Files.readBytes(f0);
        assertTrue(bs.length > 0);
        assertEquals(be.length, bs.length);
        for (int i = 0; i < bs.length; i++) {
            assertEquals(be[i], bs[i]);
        }
    }

    @Test
    public void test_2() throws IOException {
        NutMap map = doUpload("c2");

        assertEquals(1, map.size());

        byte[] be, bs;

        List<HttpFormFile> files = map.getList("f0", HttpFormFile.class);

        assertEquals(2, files.size());

        HttpFormFile p0 = files.get(0);
        assertEquals("nutz-logo_28.jpg", p0.getName());
        assertEquals("image/jpeg", p0.getContentType());
        bs = p0.getBytes();
        File f0 = Files.findFile(getMyDataFile("nutz-logo_28.jpg"));
        be = Files.readBytes(f0);
        assertTrue(bs.length > 0);
        assertEquals(be.length, bs.length);
        for (int i = 0; i < bs.length; i++) {
            assertEquals(be[i], bs[i]);
        }

        HttpFormFile p1 = files.get(1);
        assertEquals("nutz-logo_28.png", p1.getName());
        assertEquals("image/png", p1.getContentType());
        bs = p1.getBytes();
        File f1 = Files.findFile(getMyDataFile("nutz-logo_28.png"));
        be = Files.readBytes(f1);
        assertTrue(bs.length > 0);
        assertEquals(be.length, bs.length);
        for (int i = 0; i < bs.length; i++) {
            assertEquals(be[i], bs[i]);
        }

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
        bs = p1.getBytes();
        File f1 = Files.findFile(getMyDataFile("c1_p1.jpg"));
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
