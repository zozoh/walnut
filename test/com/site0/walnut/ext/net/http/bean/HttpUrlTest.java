package com.site0.walnut.ext.net.http.bean;

import static org.junit.Assert.*;

import java.net.URLEncoder;

import org.junit.Test;
import org.nutz.lang.Encoding;

public class HttpUrlTest {

    @Test
    public void test_full() {
        String str = "http://zozoh.com:8080/a/b/?x=100&y=99&nm=";
        String nm = "三个字儿";
        String encodedNm = URLEncoder.encode(nm, Encoding.CHARSET_UTF8);
        String input = str + nm + "#main";
        String encodedInput = str + encodedNm + "#main";

        HttpUrl url = new HttpUrl(input);
        assertEquals("http", url.getProtocol());
        assertEquals("zozoh.com", url.getHost());
        assertTrue(url.isPort(8080));
        assertTrue(url.hasPath());
        assertTrue(url.hasQuery());
        assertTrue(url.hasAnchor());
        assertEquals("/a/b/", url.getPath());
        assertEquals(100, url.getQuery().getInt("x"));
        assertEquals(99, url.getQuery().getInt("y"));
        assertEquals("三个字儿", url.getQuery().getString("nm"));
        assertEquals("main", url.getAnchor());

        assertEquals(input, url.toString(false, true));
        assertEquals(encodedInput, url.toString());

        url = new HttpUrl(encodedInput);
        assertEquals("http", url.getProtocol());
        assertEquals("zozoh.com", url.getHost());
        assertTrue(url.isPort(8080));
        assertTrue(url.hasPath());
        assertTrue(url.hasQuery());
        assertTrue(url.hasAnchor());
        assertEquals("/a/b/", url.getPath());
        assertEquals(100, url.getQuery().getInt("x"));
        assertEquals(99, url.getQuery().getInt("y"));
        assertEquals("三个字儿", url.getQuery().getString("nm"));
        assertEquals("main", url.getAnchor());

        assertEquals(input, url.toString(false, true));
        assertEquals(encodedInput, url.toString());
    }

    @Test
    public void test_simple() {
        String input = "http://zozoh.com";
        HttpUrl url = new HttpUrl(input);
        assertEquals("http", url.getProtocol());
        assertEquals("zozoh.com", url.getHost());
        assertTrue(url.isPort(80));
        assertFalse(url.hasPath());
        assertFalse(url.hasQuery());
        assertFalse(url.hasAnchor());
        assertEquals(input, url.toString());
    }

}
