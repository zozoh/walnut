package org.nutz.walnut.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class WsTest {

    @Test
    public void test_camelCase() {
        assertEquals("abc", Ws.camelCase("ABC"));
        assertEquals("aBc", Ws.camelCase("aBC"));
        assertEquals("aBC", Ws.camelCase("  A  b c  "));
        assertEquals("aBC", Ws.camelCase("a-b-c"));
        assertEquals("aBC", Ws.camelCase("A-b-C"));
        assertEquals("aBC", Ws.camelCase("A-_b-C"));
        assertEquals("aBC", Ws.camelCase("A_b_C"));
    }

    @Test
    public void test_kebabCase() {
        assertEquals("abc", Ws.kebabCase("ABC"));
        assertEquals("a-bc", Ws.kebabCase("aBC"));
        assertEquals("a-b-c", Ws.kebabCase("  A  b c  "));
        assertEquals("a-b-c", Ws.kebabCase("a-b-c"));
        assertEquals("a-b-c", Ws.kebabCase("a-_b-c"));
        assertEquals("a-b-c", Ws.kebabCase("A-b-C"));
        assertEquals("a-b-c", Ws.kebabCase("A_b_C"));
    }

    @Test
    public void test_snakeCase() {
        assertEquals("abc", Ws.snakeCase("ABC"));
        assertEquals("a_bc", Ws.snakeCase("aBC"));
        assertEquals("a_b_c", Ws.snakeCase("  A  b c  "));
        assertEquals("a_b_c", Ws.snakeCase("a-b-c"));
        assertEquals("a_b_c", Ws.snakeCase("A-b-C"));
        assertEquals("a_b_c", Ws.snakeCase("A-_b-C"));
        assertEquals("a_b_c", Ws.snakeCase("A_b_C"));
    }

    @Test
    public void test_intToChineseNumber() {
        assertEquals("零", Ws.intToChineseNumber(0));
        assertEquals("一", Ws.intToChineseNumber(1));
        assertEquals("二", Ws.intToChineseNumber(2));
        assertEquals("九", Ws.intToChineseNumber(9));
        assertEquals("十", Ws.intToChineseNumber(10));
        assertEquals("十一", Ws.intToChineseNumber(11));
        assertEquals("十九", Ws.intToChineseNumber(19));
        assertEquals("三十", Ws.intToChineseNumber(30));
        assertEquals("五十九", Ws.intToChineseNumber(59));
        assertEquals("一百", Ws.intToChineseNumber(100));
        assertEquals("一百一十", Ws.intToChineseNumber(110));
        assertEquals("三百一十七", Ws.intToChineseNumber(317));
        assertEquals("三百零五", Ws.intToChineseNumber(305));
        assertEquals("八千", Ws.intToChineseNumber(8000));
        assertEquals("八千零四十", Ws.intToChineseNumber(8040));
        assertEquals("三千零九", Ws.intToChineseNumber(3009));
        assertEquals("七百五十万零六十一", Ws.intToChineseNumber(7500061));
        assertEquals("七亿三千零九", Ws.intToChineseNumber(700003009));

        assertEquals("负七百", Ws.intToChineseNumber(-700));
    }

    @Test
    public void test_chineseNumberToInt() {
        assertEquals(0, Ws.chineseNumberToInt("零"));
        assertEquals(1, Ws.chineseNumberToInt("一"));
        assertEquals(2, Ws.chineseNumberToInt("二"));
        assertEquals(9, Ws.chineseNumberToInt("九"));
        assertEquals(10, Ws.chineseNumberToInt("十"));
        assertEquals(11, Ws.chineseNumberToInt("十一"));
        assertEquals(19, Ws.chineseNumberToInt("十九"));
        assertEquals(30, Ws.chineseNumberToInt("三十"));
        assertEquals(59, Ws.chineseNumberToInt("五十九"));
        assertEquals(100, Ws.chineseNumberToInt("一百"));
        assertEquals(110, Ws.chineseNumberToInt("一百一十"));
        assertEquals(317, Ws.chineseNumberToInt("三百一十七"));
        assertEquals(305, Ws.chineseNumberToInt("三百零五"));
        assertEquals(8000, Ws.chineseNumberToInt("八千"));
        assertEquals(8040, Ws.chineseNumberToInt("八千零四十"));
        assertEquals(3009, Ws.chineseNumberToInt("三千零九"));
        assertEquals(7500061, Ws.chineseNumberToInt("七百五十万零六十一"));
        assertEquals(700003009, Ws.chineseNumberToInt("七亿三千零九"));

        assertEquals(-700, Ws.chineseNumberToInt("负七百"));
    }

    @Test
    public void test_escape_unescape() {
        String input = "a\nb\r\tc";

        String s = Ws.escape(input);
        assertEquals("a\\nb\\r\\tc", s);

        String s2 = Ws.unescape(s);
        assertEquals(input, s2);
    }

}
