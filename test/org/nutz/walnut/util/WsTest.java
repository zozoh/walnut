package org.nutz.walnut.util;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.nutz.lang.Lang;
import org.nutz.lang.util.NutBean;
import org.nutz.walnut.util.callback.WnStrToken;
import org.nutz.walnut.util.callback.WnStrTokenCallback;

public class WsTest {

    @Test
    public void test_splitAttrMap2() {
        NutBean map = Ws.splitAttrMap("style=color:red; align=center contenteditable");
        assertEquals("color:red;", map.get("style"));
        assertEquals("center", map.get("align"));
        assertTrue(map.containsKey("contenteditable"));
        assertNull(map.get("contenteditable"));
    }

    @Test
    public void test_splitAttrMap() {
        NutBean map = Ws.splitAttrMap("style=\"color:red;\" align=\"center\" contenteditable");
        assertEquals("color:red;", map.get("style"));
        assertEquals("center", map.get("align"));
        assertTrue(map.containsKey("contenteditable"));
        assertNull(map.get("contenteditable"));
    }

    @Test
    public void test_splitQuoteToken() {
        String str;
        List<String> list = new ArrayList<>(10);
        WnStrTokenCallback callback = new WnStrTokenCallback() {
            public char escape(char c) {
                return Cmds.escapeChar(c);
            }

            public void invoke(WnStrToken token) {
                switch (token.type) {
                // 普通文字
                case TEXT:
                    list.add(token.text.toString());
                    break;
                // 引号
                case QUOTE:
                    String s = String.format("%s%s%s", token.quoteC, token.text, token.quoteC);
                    list.add(s);
                    break;
                // 其他的就是错误
                default:
                    throw Lang.impossible();
                }
            }
        };
        // ------------------------------------------------
        list.clear();
        Ws.splitQuoteToken("A'x'B`'f'`C", "`'", null, callback);
        str = Ws.join(list, ";");
        assertEquals("A;'x';B;`'f'`;C", str);
        // ------------------------------------------------
        list.clear();
        Ws.splitQuoteToken("A''x''B``C", "`'", null, callback);
        str = Ws.join(list, ";");
        assertEquals("A;'';x;'';B;``;C", str);
        // ------------------------------------------------
        list.clear();
        Ws.splitQuoteToken("A\\'x\\'B\\`C", "`'", null, callback);
        str = Ws.join(list, ";");
        assertEquals("A'x'B`C", str);
        // ------------------------------------------------
    }

    @Test
    public void test_fromR26Str() {
        assertEquals(1, Ws.fromR26Str("A"));
        assertEquals(2, Ws.fromR26Str("B"));
        assertEquals(3, Ws.fromR26Str("C"));
        assertEquals(4, Ws.fromR26Str("D"));
        assertEquals(5, Ws.fromR26Str("E"));
        assertEquals(6, Ws.fromR26Str("F"));
        assertEquals(7, Ws.fromR26Str("G"));
        assertEquals(8, Ws.fromR26Str("H"));
        assertEquals(9, Ws.fromR26Str("I"));
        assertEquals(10, Ws.fromR26Str("J"));
        assertEquals(11, Ws.fromR26Str("K"));
        assertEquals(12, Ws.fromR26Str("L"));
        assertEquals(13, Ws.fromR26Str("M"));
        assertEquals(14, Ws.fromR26Str("N"));
        assertEquals(15, Ws.fromR26Str("O"));
        assertEquals(16, Ws.fromR26Str("P"));
        assertEquals(17, Ws.fromR26Str("Q"));
        assertEquals(18, Ws.fromR26Str("R"));
        assertEquals(19, Ws.fromR26Str("S"));
        assertEquals(20, Ws.fromR26Str("T"));
        assertEquals(21, Ws.fromR26Str("U"));
        assertEquals(22, Ws.fromR26Str("V"));
        assertEquals(23, Ws.fromR26Str("W"));
        assertEquals(24, Ws.fromR26Str("X"));
        assertEquals(25, Ws.fromR26Str("Y"));
        assertEquals(26, Ws.fromR26Str("Z"));
        assertEquals(27, Ws.fromR26Str("AA"));
        assertEquals(28, Ws.fromR26Str("AB"));
        assertEquals(29, Ws.fromR26Str("AC"));
        assertEquals(30, Ws.fromR26Str("AD"));
        assertEquals(31, Ws.fromR26Str("AE"));
        assertEquals(32, Ws.fromR26Str("AF"));
        assertEquals(33, Ws.fromR26Str("AG"));
        assertEquals(34, Ws.fromR26Str("AH"));
        assertEquals(35, Ws.fromR26Str("AI"));
        assertEquals(36, Ws.fromR26Str("AJ"));
        assertEquals(37, Ws.fromR26Str("AK"));
        assertEquals(38, Ws.fromR26Str("AL"));
        assertEquals(39, Ws.fromR26Str("AM"));
        assertEquals(40, Ws.fromR26Str("AN"));
        assertEquals(41, Ws.fromR26Str("AO"));
        assertEquals(42, Ws.fromR26Str("AP"));
        assertEquals(43, Ws.fromR26Str("AQ"));
        assertEquals(44, Ws.fromR26Str("AR"));
        assertEquals(45, Ws.fromR26Str("AS"));
        assertEquals(46, Ws.fromR26Str("AT"));
        assertEquals(47, Ws.fromR26Str("AU"));
        assertEquals(48, Ws.fromR26Str("AV"));
        assertEquals(49, Ws.fromR26Str("AW"));
        assertEquals(50, Ws.fromR26Str("AX"));
        assertEquals(51, Ws.fromR26Str("AY"));
        assertEquals(52, Ws.fromR26Str("AZ"));
        assertEquals(53, Ws.fromR26Str("BA"));
        assertEquals(54, Ws.fromR26Str("BB"));
        assertEquals(55, Ws.fromR26Str("BC"));
        assertEquals(56, Ws.fromR26Str("BD"));
        assertEquals(57, Ws.fromR26Str("BE"));
        assertEquals(58, Ws.fromR26Str("BF"));
        assertEquals(59, Ws.fromR26Str("BG"));
        assertEquals(60, Ws.fromR26Str("BH"));
        assertEquals(61, Ws.fromR26Str("BI"));
        assertEquals(62, Ws.fromR26Str("BJ"));
        assertEquals(63, Ws.fromR26Str("BK"));
        assertEquals(64, Ws.fromR26Str("BL"));
        assertEquals(65, Ws.fromR26Str("BM"));
        assertEquals(66, Ws.fromR26Str("BN"));
        assertEquals(67, Ws.fromR26Str("BO"));
        assertEquals(68, Ws.fromR26Str("BP"));
        assertEquals(69, Ws.fromR26Str("BQ"));
        assertEquals(70, Ws.fromR26Str("BR"));
        assertEquals(71, Ws.fromR26Str("BS"));
        assertEquals(72, Ws.fromR26Str("BT"));
        assertEquals(73, Ws.fromR26Str("BU"));
        assertEquals(74, Ws.fromR26Str("BV"));
        assertEquals(75, Ws.fromR26Str("BW"));
        assertEquals(76, Ws.fromR26Str("BX"));
        assertEquals(77, Ws.fromR26Str("BY"));
        assertEquals(78, Ws.fromR26Str("BZ"));
    }

    @Test
    public void test_toR26Str() {
        assertEquals("A", Ws.toR26Str(1));
        assertEquals("B", Ws.toR26Str(2));
        assertEquals("C", Ws.toR26Str(3));
        assertEquals("D", Ws.toR26Str(4));
        assertEquals("E", Ws.toR26Str(5));
        assertEquals("F", Ws.toR26Str(6));
        assertEquals("G", Ws.toR26Str(7));
        assertEquals("H", Ws.toR26Str(8));
        assertEquals("I", Ws.toR26Str(9));
        assertEquals("J", Ws.toR26Str(10));
        assertEquals("K", Ws.toR26Str(11));
        assertEquals("L", Ws.toR26Str(12));
        assertEquals("M", Ws.toR26Str(13));
        assertEquals("N", Ws.toR26Str(14));
        assertEquals("O", Ws.toR26Str(15));
        assertEquals("P", Ws.toR26Str(16));
        assertEquals("Q", Ws.toR26Str(17));
        assertEquals("R", Ws.toR26Str(18));
        assertEquals("S", Ws.toR26Str(19));
        assertEquals("T", Ws.toR26Str(20));
        assertEquals("U", Ws.toR26Str(21));
        assertEquals("V", Ws.toR26Str(22));
        assertEquals("W", Ws.toR26Str(23));
        assertEquals("X", Ws.toR26Str(24));
        assertEquals("Y", Ws.toR26Str(25));
        assertEquals("Z", Ws.toR26Str(26));
        assertEquals("AA", Ws.toR26Str(27));
        assertEquals("AB", Ws.toR26Str(28));
        assertEquals("AC", Ws.toR26Str(29));
        assertEquals("AD", Ws.toR26Str(30));
        assertEquals("AE", Ws.toR26Str(31));
        assertEquals("AF", Ws.toR26Str(32));
        assertEquals("AG", Ws.toR26Str(33));
        assertEquals("AH", Ws.toR26Str(34));
        assertEquals("AI", Ws.toR26Str(35));
        assertEquals("AJ", Ws.toR26Str(36));
        assertEquals("AK", Ws.toR26Str(37));
        assertEquals("AL", Ws.toR26Str(38));
        assertEquals("AM", Ws.toR26Str(39));
        assertEquals("AN", Ws.toR26Str(40));
        assertEquals("AO", Ws.toR26Str(41));
        assertEquals("AP", Ws.toR26Str(42));
        assertEquals("AQ", Ws.toR26Str(43));
        assertEquals("AR", Ws.toR26Str(44));
        assertEquals("AS", Ws.toR26Str(45));
        assertEquals("AT", Ws.toR26Str(46));
        assertEquals("AU", Ws.toR26Str(47));
        assertEquals("AV", Ws.toR26Str(48));
        assertEquals("AW", Ws.toR26Str(49));
        assertEquals("AX", Ws.toR26Str(50));
        assertEquals("AY", Ws.toR26Str(51));
        assertEquals("AZ", Ws.toR26Str(52));
        assertEquals("BA", Ws.toR26Str(53));
        assertEquals("BB", Ws.toR26Str(54));
        assertEquals("BC", Ws.toR26Str(55));
        assertEquals("BD", Ws.toR26Str(56));
        assertEquals("BE", Ws.toR26Str(57));
        assertEquals("BF", Ws.toR26Str(58));
        assertEquals("BG", Ws.toR26Str(59));
        assertEquals("BH", Ws.toR26Str(60));
        assertEquals("BI", Ws.toR26Str(61));
        assertEquals("BJ", Ws.toR26Str(62));
        assertEquals("BK", Ws.toR26Str(63));
        assertEquals("BL", Ws.toR26Str(64));
        assertEquals("BM", Ws.toR26Str(65));
        assertEquals("BN", Ws.toR26Str(66));
        assertEquals("BO", Ws.toR26Str(67));
        assertEquals("BP", Ws.toR26Str(68));
        assertEquals("BQ", Ws.toR26Str(69));
        assertEquals("BR", Ws.toR26Str(70));
        assertEquals("BS", Ws.toR26Str(71));
        assertEquals("BT", Ws.toR26Str(72));
        assertEquals("BU", Ws.toR26Str(73));
        assertEquals("BV", Ws.toR26Str(74));
        assertEquals("BW", Ws.toR26Str(75));
        assertEquals("BX", Ws.toR26Str(76));
        assertEquals("BY", Ws.toR26Str(77));
        assertEquals("BZ", Ws.toR26Str(78));

    }

    @Test
    public void test_decodeUnicode() {
        assertEquals("A‘X’C", Ws.decodeUnicode("A\u2018X\u2019C"));
        assertEquals("测‘试’行", Ws.decodeUnicode("测\u2018试\u2019行"));

        assertEquals("A–X–C", Ws.decodeUnicode("A\u2013X\u2013C"));
        assertEquals("测–试–行", Ws.decodeUnicode("测\u2013试\u2013行"));

        assertEquals("A“X”C", Ws.decodeUnicode("A\u201cX\u201dC"));
        assertEquals("测“试”行", Ws.decodeUnicode("测\u201c试\u201d行"));
    }

    @Test
    public void test_headerCase() {
        assertEquals("Abc", Ws.headerCase("ABC"));
        assertEquals("A-Bc", Ws.headerCase("aBC"));
        assertEquals("A-B-C", Ws.headerCase("  A  b c  "));
        assertEquals("A-B-C", Ws.headerCase("a-b-c"));
        assertEquals("A-B-C", Ws.headerCase("a-_b-c"));
        assertEquals("A-B-C", Ws.headerCase("A-b-C"));
        assertEquals("A-B-C", Ws.headerCase("A_b_C"));
        assertEquals("Content-Type", Ws.headerCase("content_type"));
        assertEquals("Content-Type", Ws.headerCase("content-type"));
        assertEquals("Content-Type", Ws.headerCase("contentType"));
    }

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
        assertEquals("content-type", Ws.kebabCase("content_type"));
        assertEquals("content-type", Ws.kebabCase("content-type"));
        assertEquals("content-type", Ws.kebabCase("Content-Type"));
        assertEquals("content-type", Ws.kebabCase("contentType"));
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
