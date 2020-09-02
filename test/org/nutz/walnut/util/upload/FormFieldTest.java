package org.nutz.walnut.util.upload;

import static org.junit.Assert.*;

import org.junit.Test;

public class FormFieldTest {

    @Test
    public void test_file_head() {
        String s = "Content-Disposition: form-data; name=\"f1\"; filename=\"hello.txt\"\r\n";
        s += "Content-Type: text/plain";
        FormField ff = new FormField(s);
        assertTrue(ff.isFile());
        assertEquals("f1", ff.getName());
        assertEquals("hello.txt", ff.getFileName());
        assertEquals("text/plain", ff.getContentType());
    }

    @Test
    public void test_file_head_s0() {
        String s = "Content-Disposition: form-data;";
        s += " name=\"f1\"; filename=\"h\\\"e\\\"ll\\\"o\\\".txt\"\r\n";
        s += "Content-Type: text/plain";
        FormField ff = new FormField(s);
        assertTrue(ff.isFile());
        assertEquals("f1", ff.getName());
        assertEquals("h\"e\"ll\"o\".txt", ff.getFileName());
        assertEquals("text/plain", ff.getContentType());
    }

    @Test
    public void test_text_head() {
        String s = "Content-Disposition: form-data; name=\"age\"";
        FormField ff = new FormField(s);
        assertTrue(ff.isText());
        assertEquals("age", ff.getName());
        assertNull(ff.getFileName());
        assertNull(ff.getContentType());
    }

    @Test
    public void test_text_head_s0() {
        String s = "Content-Disposition: form-data; name=\"a;\\\"g\\\"\\\"e\\\"\"";
        FormField ff = new FormField(s);
        assertTrue(ff.isText());
        assertEquals("a;\"g\"\"e\"", ff.getName());
        assertNull(ff.getFileName());
        assertNull(ff.getContentType());
    }

}
