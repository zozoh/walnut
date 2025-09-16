package com.site0.walnut.ext.net.http.bean;

import static org.junit.Assert.*;

import org.junit.Test;

public class HttpContentDispositionTest {

    @Test
    public void testParseWithOnlyType() {
        HttpContentDisposition disposition = HttpContentDisposition.parse("attachment");
        assertNotNull(disposition);
        assertEquals("attachment", disposition.getType());
        assertTrue(disposition.isAttachment());
        assertFalse(disposition.isInline());
        assertNull(disposition.getFilename());
        assertNull(disposition.getFilenameDecoded());
        assertNull(disposition.getPreferredFilename());
    }
    
    @Test
    public void testParseWithInlineType() {
        HttpContentDisposition disposition = HttpContentDisposition.parse("inline");
        assertNotNull(disposition);
        assertEquals("inline", disposition.getType());
        assertFalse(disposition.isAttachment());
        assertTrue(disposition.isInline());
    }
    
    @Test
    public void testParseWithFilename() {
        HttpContentDisposition disposition = HttpContentDisposition.parse("attachment; filename=example.txt");
        assertNotNull(disposition);
        assertEquals("attachment", disposition.getType());
        assertEquals("example.txt", disposition.getFilename());
        assertEquals("example.txt", disposition.getPreferredFilename());
        assertNull(disposition.getFilenameDecoded());
    }
    
    @Test
    public void testParseWithQuotedFilename() {
        HttpContentDisposition disposition = HttpContentDisposition.parse("attachment; filename=\"example with spaces.txt\"");
        assertNotNull(disposition);
        assertEquals("attachment", disposition.getType());
        assertEquals("example with spaces.txt", disposition.getFilename());
    }
    
    @Test
    public void testParseWithFilenameStar() {
        String encodedFilename = "utf-8''%E4%B8%AD%E6%96%87%E6%96%87%E4%BB%B6.txt"; // 中文文件.txt
        HttpContentDisposition disposition = HttpContentDisposition.parse("attachment; filename*=" + encodedFilename);
        assertNotNull(disposition);
        assertEquals("attachment", disposition.getType());
        assertEquals("中文文件.txt", disposition.getFilenameDecoded());
        assertEquals("中文文件.txt", disposition.getPreferredFilename());
        assertNull(disposition.getFilename());
    }
    
    @Test
    public void testParseWithBothFilenames() {
        String encodedFilename = "utf-8''%E4%B8%AD%E6%96%87%E6%96%87%E4%BB%B6.txt"; // 中文文件.txt
        HttpContentDisposition disposition = HttpContentDisposition.parse(
                "attachment; filename=fallback.txt; filename*=" + encodedFilename);
        assertNotNull(disposition);
        assertEquals("attachment", disposition.getType());
        assertEquals("fallback.txt", disposition.getFilename());
        assertEquals("中文文件.txt", disposition.getFilenameDecoded());
        // 验证优先使用filenameDecoded
        assertEquals("中文文件.txt", disposition.getPreferredFilename());
    }
    
    @Test
    public void testParseWithAdditionalParams() {
        HttpContentDisposition disposition = HttpContentDisposition.parse(
                "attachment; filename=example.txt; size=1024; creation-date=2023-01-01");
        assertNotNull(disposition);
        assertEquals("attachment", disposition.getType());
        assertEquals("example.txt", disposition.getFilename());
        
        // 验证参数映射
        assertNotNull(disposition.getParams());
        assertEquals("1024", disposition.getParams().getString("size"));
        assertEquals("2023-01-01", disposition.getParams().getString("creation-date"));
    }
    
    @Test
    public void testParseWithWhitespace() {
        HttpContentDisposition disposition = HttpContentDisposition.parse(
                "  attachment  ;  filename  =  example.txt  ");
        assertNotNull(disposition);
        assertEquals("attachment", disposition.getType());
        assertEquals("example.txt", disposition.getFilename());
    }
    
    @Test
    public void testParseWithNull() {
        HttpContentDisposition disposition = HttpContentDisposition.parse(null);
        assertNull(disposition);
    }
    
    @Test
    public void testParseWithEmptyString() {
        HttpContentDisposition disposition = HttpContentDisposition.parse("");
        assertNull(disposition);
    }
    
    @Test
    public void testParseWithWhitespaceOnly() {
        HttpContentDisposition disposition = HttpContentDisposition.parse("   ");
        assertNull(disposition);
    }
    
    @Test
    public void testFilenameStarDecodingWithoutCharset() {
        // 没有指定charset的情况
        String encodedFilename = "''%E4%B8%AD%E6%96%87%E6%96%87%E4%BB%B6.txt"; // 中文文件.txt
        HttpContentDisposition disposition = HttpContentDisposition.parse("attachment; filename*=" + encodedFilename);
        assertNotNull(disposition);
        assertEquals("中文文件.txt", disposition.getFilenameDecoded());
    }
    
    @Test
    public void testFilenameStarDecodingWithSpecialChars() {
        // 包含特殊字符的文件名
        String encodedFilename = "utf-8''%E4%B8%AD%E6%96%87%20%21%40%23%24%25%5E%26%2A%28%29.txt"; // 中文 !@#$%^&*().txt
        HttpContentDisposition disposition = HttpContentDisposition.parse("attachment; filename*=" + encodedFilename);
        assertNotNull(disposition);
        assertEquals("中文 !@#$%^&*().txt", disposition.getFilenameDecoded());
    }
    
    @Test
    public void testFilenameStarDecodingWithLanguage() {
        // 包含language参数的情况
        String encodedFilename = "utf-8'en'%E4%B8%AD%E6%96%87%E6%96%87%E4%BB%B6.txt"; // 中文文件.txt
        HttpContentDisposition disposition = HttpContentDisposition.parse("attachment; filename*=" + encodedFilename);
        assertNotNull(disposition);
        assertEquals("中文文件.txt", disposition.getFilenameDecoded());
    }
    
    @Test
    public void testFilenameStarDecodingNonStandardFormat() {
        // 非标准格式，直接尝试URL解码
        String encodedFilename = "%E4%B8%AD%E6%96%87%E6%96%87%E4%BB%B6.txt"; // 中文文件.txt
        HttpContentDisposition disposition = HttpContentDisposition.parse("attachment; filename*=" + encodedFilename);
        assertNotNull(disposition);
        assertEquals("中文文件.txt", disposition.getFilenameDecoded());
    }
    
    @Test
    public void testToStringMethod() {
        HttpContentDisposition disposition = HttpContentDisposition.parse(
                "attachment; filename=example.txt; size=1024");
        assertNotNull(disposition);
        String toStringResult = disposition.toString();
        assertNotNull(toStringResult);
        assertTrue(toStringResult.contains("attachment"));
        assertTrue(toStringResult.contains("filename=example.txt"));
        assertTrue(toStringResult.contains("size=1024"));
    }
    
    @Test
    public void testToStringWithQuotedValues() {
        HttpContentDisposition disposition = HttpContentDisposition.parse(
                "attachment; filename=\"example; with; semicolons.txt\"");
        assertNotNull(disposition);
        String toStringResult = disposition.toString();
        assertNotNull(toStringResult);
        assertTrue(toStringResult.contains("filename=\"example; with; semicolons.txt\""));
    }
    
    @Test
    public void testCaseInsensitivity() {
        // 测试大小写不敏感
        HttpContentDisposition disposition = HttpContentDisposition.parse(
                "ATTACHMENT; FILENAME=example.txt; FILENAME*=utf-8''%E4%B8%AD%E6%96%87%E6%96%87%E4%BB%B6.txt");
        assertNotNull(disposition);
        assertEquals("attachment", disposition.getType());
        assertEquals("example.txt", disposition.getFilename());
        assertEquals("中文文件.txt", disposition.getFilenameDecoded());
    }
    
    @Test
    public void testMalformedHeader() {
        // 测试格式不正确的头部，应该尽可能解析
        HttpContentDisposition disposition = HttpContentDisposition.parse(
                "attachment; invalid_param; filename=example.txt");
        assertNotNull(disposition);
        assertEquals("attachment", disposition.getType());
        assertEquals("example.txt", disposition.getFilename());
    }
}
