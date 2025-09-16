package com.site0.walnut.ext.net.http.bean;

import java.net.URLDecoder;
import java.util.List;
import java.util.Map;

import org.nutz.lang.Encoding;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.util.Ws;

/**
 * 解析HTTP Content-Disposition头部的工具类 支持标准的filename参数和RFC 5987定义的filename*参数
 * 
 * @author AI Assistant
 */
public class HttpContentDisposition {

    // Content-Disposition类型，如'attachment'或'inline'
    private String type;

    // 原始文件名参数值
    private String filename;

    // filename*参数解码后的值
    private String filenameDecoded;

    // 所有参数的映射表
    private NutMap params;

    /**
     * 私有构造函数
     */
    private HttpContentDisposition() {
        this.params = new NutMap();
    }

    /**
     * 解析Content-Disposition头部字符串
     * 
     * @param headerValue
     *            Content-Disposition头部的值
     * @return 解析后的HttpContentDisposition对象
     */
    public static HttpContentDisposition parse(String headerValue) {
        if (Ws.isBlank(headerValue)) {
            return null;
        }

        HttpContentDisposition disposition = new HttpContentDisposition();

        // 使用splitQuote方法按照分号分割字符串，正确处理引号内的分号
        List<String> parts = Ws
            .splitQuote(headerValue, "\"", '\\', true, true, ";");
        if (parts != null && !parts.isEmpty()) {
            // 第一个部分是类型
            disposition.type = parts.get(0).trim().toLowerCase();

            // 处理其余部分作为参数
            for (int i = 1; i < parts.size(); i++) {
                String part = parts.get(i).trim();
                if (Ws.isBlank(part)) {
                    continue;
                }
                // 查找等号位置
                int eqIdx = part.indexOf('=');
                if (eqIdx > 0) {
                    // 解析键值对
                    String key = part.substring(0, eqIdx).trim().toLowerCase();
                    String value = part.substring(eqIdx + 1).trim();

                    // 处理带引号的值
                    if (value.startsWith("\"")
                        && value.endsWith("\"")
                        && value.length() > 1) {
                        value = value.substring(1, value.length() - 1);
                    }

                    disposition.params.put(key, value);

                    // 处理特殊参数
                    if ("filename".equals(key)) {
                        disposition.filename = value;
                    } else if ("filename*".equals(key)) {
                        disposition.filenameDecoded = decodeFilenameStar(value);
                    }
                }
            }
        }

        return disposition;
    }

    /**
     * 解码filename*参数的值 格式: charset'language'encoded-text
     * 
     * @param value
     *            filename*参数的值
     * @return 解码后的文件名
     */
    private static String decodeFilenameStar(String value) {
        if (Ws.isBlank(value)) {
            return null;
        }

        try {
            // 分割字符串，查找两个单引号的位置
            int firstQuotePos = value.indexOf('\'');
            if (firstQuotePos >= 0) {
                int secondQuotePos = value.indexOf('\'', firstQuotePos + 1);
                if (secondQuotePos > firstQuotePos) {
                    // 提取编码后的文本部分（从第二个单引号之后开始）
                    String encodedText = value.substring(secondQuotePos + 1);

                    if (!Ws.isBlank(encodedText)) {
                        // 提取字符集
                        String charset = value.substring(0, firstQuotePos)
                            .trim();
                        if (Ws.isBlank(charset)) {
                            charset = "UTF-8";
                        }

                        try {
                            // 直接对编码文本进行URL解码
                            return URLDecoder.decode(encodedText, charset);
                        }
                        catch (Exception e) {
                            // 如果解码失败，尝试使用UTF-8
                            try {
                                return URLDecoder.decode(encodedText, "UTF-8");
                            }
                            catch (Exception e2) {
                                // 如果仍然失败，返回原始编码文本
                                return encodedText;
                            }
                        }
                    }
                }
            }
            // 默认按照 UTF-8 解码
            return URLDecoder.decode(value, Encoding.CHARSET_UTF8);
        }
        catch (Exception e) {
            // 捕获所有异常，确保方法不会因解析错误而失败
        }

        // 如果格式不匹配或解析失败，返回原始值
        return value;
    }

    /**
     * 获取Content-Disposition类型
     */
    public String getType() {
        return type;
    }

    /**
     * 获取原始filename参数的值
     */
    public String getFilename() {
        return filename;
    }

    /**
     * 获取filename*参数解码后的值
     */
    public String getFilenameDecoded() {
        return filenameDecoded;
    }

    /**
     * 获取首选的文件名（优先使用filename*）
     */
    public String getPreferredFilename() {
        return Ws.sBlank(filenameDecoded, filename);
    }

    /**
     * 获取所有参数
     */
    public NutMap getParams() {
        return params;
    }

    /**
     * 检查是否为附件类型
     */
    public boolean isAttachment() {
        return "attachment".equals(type);
    }

    /**
     * 检查是否为内联类型
     */
    public boolean isInline() {
        return "inline".equals(type);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(type);

        for (Map.Entry<String, Object> entry : params.entrySet()) {
            sb.append("; ").append(entry.getKey()).append("=");
            String value = entry.getValue().toString();
            // 如果值包含特殊字符，添加引号
            if (value.contains(",")
                || value.contains(":")
                || value.contains(";")) {
                sb.append('"').append(value).append('"');
            } else {
                sb.append(value);
            }
        }

        return sb.toString();
    }
}
