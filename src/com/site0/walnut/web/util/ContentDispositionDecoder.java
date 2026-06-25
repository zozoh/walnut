package com.site0.walnut.web.util;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 解析和构建 HTTP Content-Disposition 头中的 filename 参数。
 * <p>
 * 支持 RFC 5987 (filename*=charset'lang'encoded) 和 RFC 2616 (filename="...")
 * 两种格式。
 * 
 * @author zozoh
 */
public class ContentDispositionDecoder {

    /**
     * 匹配 RFC 5987 格式: filename*=charset'lang'encoded-value encoded-value
     * 只包含合法字符，遇到 ; 空格 " 等分隔符自动停止
     */
    private static final Pattern RFC5987_PATTERN = Pattern
        .compile("filename\\*\\s*=\\s*([^']+)'([^']*)'([%A-Za-z0-9._~!$&'()*+,=:@/?-]*)");

    /**
     * 匹配普通格式: filename="..." 或 filename=...（无引号）
     */
    private static final Pattern PLAIN_PATTERN = Pattern
        .compile("filename\\s*=\\s*(?:\"([^\"]*)\"|([^\\s;]*))");

    /**
     * 从 Content-Disposition 字符串中提取并解码文件名。
     * <p>
     * 优先尝试 RFC 5987 编码 (filename*=...)，失败则回退到普通格式 (filename="...")。
     * 
     * @param contentDisposition
     *            Content-Disposition 头值，可为 null
     * @return 解码后的文件名，解析失败返回 null
     */
    public static String decode(String contentDisposition) {
        if (contentDisposition == null || contentDisposition.isEmpty()) {
            return null;
        }

        // 优先尝试解析 filename*=...（RFC 5987 编码）
        String rfc5987Name = decodeRfc5987(contentDisposition);
        if (rfc5987Name != null) {
            return rfc5987Name;
        }

        // 回退到普通 filename="..."
        return decodePlainFilename(contentDisposition);
    }

    /**
     * 解析 RFC 5987 格式: filename*=charset'lang'encoded-value
     */
    private static String decodeRfc5987(String header) {
        Matcher matcher = RFC5987_PATTERN.matcher(header);
        if (!matcher.find()) {
            return null;
        }

        String charset = matcher.group(1).trim();
        String encodedValue = matcher.group(3).trim();

        // 空编码值返回空字符串
        if (encodedValue.isEmpty()) {
            return "";
        }

        try {
            // 检查是否存在不完整的 % 编码
            if (hasInvalidPercentEncoding(encodedValue)) {
                return null;
            }

            String result = URLDecoder.decode(encodedValue, charset);

            // 检查解码结果是否包含替换字符（解码失败）
            if (result.contains("\uFFFD")) {
                return null;
            }

            return result;
        }
        catch (Exception e) {
            return null;
        }
    }

    /**
     * 检查是否存在不完整的 % 编码（% 后面不是两位十六进制）
     */
    private static boolean hasInvalidPercentEncoding(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (value.charAt(i) == '%') {
                // % 后面必须跟至少两位
                if (i + 2 >= value.length()) {
                    return true;
                }
                char c1 = value.charAt(i + 1);
                char c2 = value.charAt(i + 2);
                if (!isHexDigit(c1) || !isHexDigit(c2)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isHexDigit(char c) {
        return (c >= '0' && c <= '9')
               || (c >= 'A' && c <= 'F')
               || (c >= 'a' && c <= 'f');
    }

    /**
     * 解析普通格式: filename="name.txt" 或 filename=name.txt
     */
    private static String decodePlainFilename(String header) {
        Matcher matcher = PLAIN_PATTERN.matcher(header);
        if (matcher.find()) {
            // group(1) 是带引号的捕获，group(2) 是不带引号的捕获
            String filename = matcher.group(1) != null ? matcher.group(1)
                                                       : matcher.group(2);
            return filename;
        }
        return null;
    }

    // ==================== 编码构建方法 ====================

    /**
     * 构建 RFC 5987 编码的 Content-Disposition filename 参数。
     * <p>
     * 自动检测是否需要编码：纯 ASCII 直接返回，非 ASCII 进行 RFC 5987 编码。
     * 
     * @param filename
     *            原始文件名
     * @return 编码后的 filename 参数值，格式为 filename*=UTF-8''... 或 filename="..."
     */
    public static String encode(String filename) {
        if (filename == null) {
            return null;
        }
        if (filename.isEmpty()) {
            return "filename*=UTF-8''";
        }

        boolean needsEncoding = filename.chars().anyMatch(c -> c > 127);

        if (needsEncoding) {
            String encoded = java.net.URLEncoder
                .encode(filename, StandardCharsets.UTF_8)
                .replace("+", "%20");
            return "filename*=UTF-8''" + encoded;
        }

        return "filename=\"" + filename + "\"";
    }

    /**
     * 构建完整的 Content-Disposition 头值（包含 disposition type）。
     * 
     * @param dispositionType
     *            如 "attachment" 或 "inline"
     * @param filename
     *            文件名
     * @return 完整的 Content-Disposition 头值
     */
    public static String buildHeader(String dispositionType, String filename) {
        if (dispositionType == null || dispositionType.isEmpty()) {
            dispositionType = "attachment";
        }
        return dispositionType + "; " + encode(filename);
    }
}