package com.site0.walnut.web.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class ContentDispositionDecoderTest {

    // ==================== RFC 5987 编码格式 (filename*=...) ====================

    @Test
    public void testRfc5987_ChineseFilename() {
        String header = "filename*=UTF-8''%E6%B5%8B%E8%AF%95%E6%8A%A5%E5%91%8A.pdf";
        String result = ContentDispositionDecoder.decode(header);
        assertEquals("测试报告.pdf", result);
    }

    @Test
    public void testRfc5987_ChineseWithSpaces() {
        String header = "filename*=UTF-8''%E6%88%91%E7%9A%84%20%E6%96%87%E4%BB%B6.txt";
        String result = ContentDispositionDecoder.decode(header);
        assertEquals("我的 文件.txt", result);
    }

    @Test
    public void testRfc5987_JapaneseFilename() {
        String header = "filename*=UTF-8''%E3%83%86%E3%82%B9%E3%83%88.pdf";
        String result = ContentDispositionDecoder.decode(header);
        assertEquals("テスト.pdf", result);
    }

    @Test
    public void testRfc5987_GermanWithLanguageTag() {
        // 带语言标签的德语文件名
        String header = "filename*=UTF-8'de'%C3%9Cbersicht.pdf";
        String result = ContentDispositionDecoder.decode(header);
        assertEquals("Übersicht.pdf", result);
    }

    @Test
    public void testRfc5987_EmojiFilename() {
        String header = "filename*=UTF-8''%F0%9F%98%80.txt";
        String result = ContentDispositionDecoder.decode(header);
        assertEquals("😀.txt", result);
    }

    @Test
    public void testRfc5987_MixedAsciiAndUnicode() {
        String header = "filename*=UTF-8''report_%E4%B8%AD%E6%96%87_2024.pdf";
        String result = ContentDispositionDecoder.decode(header);
        assertEquals("report_中文_2024.pdf", result);
    }

    @Test
    public void testRfc5987_SpecialCharsInFilename() {
        // 文件名包含 & = ? 等特殊字符
        String header = "filename*=UTF-8''a%26b%3Dc%3Fd.txt";
        String result = ContentDispositionDecoder.decode(header);
        assertEquals("a&b=c?d.txt", result);
    }

    @Test
    public void testRfc5987_PlusSignHandling() {
        // 测试 + 号是否被正确处理为空格（URLDecoder 默认行为）
        String header = "filename*=UTF-8''a%2Bb.txt";
        String result = ContentDispositionDecoder.decode(header);
        assertEquals("a+b.txt", result);
    }

    @Test
    public void testRfc5987_LowercaseUtf8() {
        // charset 大小写不敏感测试
        String header = "filename*=utf-8''%E4%B8%AD%E6%96%87.pdf";
        String result = ContentDispositionDecoder.decode(header);
        assertEquals("中文.pdf", result);
    }

    @Test
    public void testRfc5987_WithWhitespace() {
        // 等号前后有空格
        String header = "filename* = UTF-8''%E4%B8%AD%E6%96%87.pdf";
        String result = ContentDispositionDecoder.decode(header);
        assertEquals("中文.pdf", result);
    }

    // ==================== 普通格式 (filename="...") ====================

    @Test
    public void testPlainAsciiFilename() {
        String header = "filename=\"report.pdf\"";
        String result = ContentDispositionDecoder.decode(header);
        assertEquals("report.pdf", result);
    }

    @Test
    public void testPlainAsciiWithoutQuotes() {
        // 无引号（不规范但常见）
        String header = "filename=report.pdf";
        String result = ContentDispositionDecoder.decode(header);
        assertEquals("report.pdf", result);
    }

    @Test
    public void testPlainFilenameWithSpaces() {
        String header = "filename=\"my report 2024.pdf\"";
        String result = ContentDispositionDecoder.decode(header);
        assertEquals("my report 2024.pdf", result);
    }

    @Test
    public void testPlainFilenameWithSemicolon() {
        // 文件名包含分号（必须用引号包裹）
        String header = "filename=\"report;final.pdf\"";
        String result = ContentDispositionDecoder.decode(header);
        assertEquals("report;final.pdf", result);
    }

    @Test
    public void testPlainEmptyFilename() {
        String header = "filename=\"\"";
        String result = ContentDispositionDecoder.decode(header);
        assertEquals("", result);
    }

    // ==================== 混合格式 (filename + filename*=) ====================

    @Test
    public void testMixed_BothPresent() {
        // 旧浏览器兼容格式：优先使用 RFC 5987
        String header = "filename=\"____.pdf\"; filename*=UTF-8''%E4%B8%AD%E6%96%87.pdf";
        String result = ContentDispositionDecoder.decode(header);
        assertEquals("中文.pdf", result);
    }

    @Test
    public void testMixed_ReverseOrder() {
        // filename*= 在前，filename= 在后
        String header = "filename*=UTF-8''%E4%B8%AD%E6%96%87.pdf; filename=\"fallback.pdf\"";
        String result = ContentDispositionDecoder.decode(header);
        assertEquals("中文.pdf", result);
    }

    @Test
    public void testMixed_OnlyPlainPresent() {
        // 只有普通 filename，没有 filename*=
        String header = "attachment; filename=\"report.pdf\"";
        String result = ContentDispositionDecoder.decode(header);
        assertEquals("report.pdf", result);
    }

    // ==================== 边界和异常场景 ====================

    @Test
    public void testNullInput() {
        String result = ContentDispositionDecoder.decode(null);
        assertNull(result);
    }

    @Test
    public void testEmptyInput() {
        String result = ContentDispositionDecoder.decode("");
        assertNull(result);
    }

    @Test
    public void testBlankInput() {
        String result = ContentDispositionDecoder.decode("   ");
        assertNull(result);
    }

    @Test
    public void testNoFilenameParameter() {
        // 没有 filename 或 filename* 参数
        String header = "attachment; size=1234";
        String result = ContentDispositionDecoder.decode(header);
        assertNull(result);
    }

    @Test
    public void testInvalidRfc5987_MissingEncodedValue() {
        // 缺少编码值
        String header = "filename*=UTF-8''";
        String result = ContentDispositionDecoder.decode(header);
        assertEquals("", result); // 空字符串解码结果
    }

    @Test
    public void testInvalidRfc5987_MissingQuotes() {
        // 错误的引号包裹（实际测试清理逻辑）
        String header = "filename*=UTF-8''%E4%B8%AD%E6%96%87.pdf\";";
        String result = ContentDispositionDecoder.decode(header);
        assertEquals("中文.pdf", result);
    }

    @Test
    public void testPlainWithTrailingSemicolon() {
        String header = "filename=\"report.pdf\";";
        String result = ContentDispositionDecoder.decode(header);
        assertEquals("report.pdf", result);
    }

    @Test
    public void testPlainWithMultipleSpaces() {
        String header = "filename  =  \"report.pdf\"  ";
        String result = ContentDispositionDecoder.decode(header);
        assertEquals("report.pdf", result);
    }

    @Test
    public void testRfc5987_UnsupportedCharset() {
        // 不支持的字符集，应回退到普通解析
        String header = "filename*=UNKNOWN-8''%E4%B8%AD%E6%96%87.pdf";
        String result = ContentDispositionDecoder.decode(header);
        // 由于 charset 不支持，回退到 plain 解析，找不到则返回 null
        assertNull(result);
    }

    @Test
    public void testRfc5987_InvalidPercentEncoding() {
        // 错误的百分号编码（% 后面不是两位十六进制）
        String header = "filename*=UTF-8''%ZZ%XX.txt";
        String result = ContentDispositionDecoder.decode(header);
        // URLDecoder 会抛出 IllegalArgumentException，被捕获后返回 null
        assertNull(result);
    }

    @Test
    public void testRfc5987_IncompletePercentEncoding() {
        // 不完整的百分号编码（% 后面只有一位）
        String header = "filename*=UTF-8''%E4.txt";
        String result = ContentDispositionDecoder.decode(header);
        assertNull(result);
    }

    @Test
    public void testVeryLongFilename() {
        // 超长文件名
        StringBuilder longName = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            longName.append("%E4%B8%AD"); // "中" 的编码
        }
        String header = "filename*=UTF-8''" + longName.toString() + ".txt";
        String result = ContentDispositionDecoder.decode(header);

        StringBuilder expected = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            expected.append("中");
        }
        assertEquals(expected.toString() + ".txt", result);
    }

    @Test
    public void testRealWorldChromeHeader() {
        // 模拟 Chrome 实际发送的 header
        String header = "attachment; filename=\"%E4%B8%AD%E6%96%87.pdf\"; filename*=UTF-8''%E4%B8%AD%E6%96%87.pdf";
        String result = ContentDispositionDecoder.decode(header);
        assertEquals("中文.pdf", result);
    }

    @Test
    public void testRealWorldSafariHeader() {
        // Safari 有时会发送这种格式
        String header = "attachment; filename=\"????.pdf\"; filename*=UTF-8''%E4%B8%AD%E6%96%87.pdf";
        String result = ContentDispositionDecoder.decode(header);
        assertEquals("中文.pdf", result);
    }
}