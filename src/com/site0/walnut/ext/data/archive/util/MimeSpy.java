package com.site0.walnut.ext.data.archive.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.nutz.lang.Streams;

/**
 * 通过 InputStream 读取文件头部字节，判断文件的 MIME 类型
 */
public class MimeSpy {
    // 1. 定义“魔数- mime类型”映射表（覆盖常见格式，可按需扩展）
    // key：文件头部关键字节（十六进制转字节数组），value：对应的 MIME 类型
    private static final Map<byte[], String> MAGIC_NUMBER_TO_MIME = new HashMap<>();

    // 初始化映射表（静态代码块，程序启动时加载）
    static {
        // 图片格式
        MAGIC_NUMBER_TO_MIME.put(hexToBytes("FFD8FF"), "image/jpeg"); // JPG/JPEG
        MAGIC_NUMBER_TO_MIME.put(hexToBytes("89504E470D0A1A0A"), "image/png");// PNG
        MAGIC_NUMBER_TO_MIME.put(hexToBytes("474946383961"), "image/gif"); // GIF89a
        MAGIC_NUMBER_TO_MIME.put(hexToBytes("474946383761"), "image/gif"); // GIF87a
        MAGIC_NUMBER_TO_MIME.put(hexToBytes("424D"), "image/bmp"); // BMP
        MAGIC_NUMBER_TO_MIME.put(hexToBytes("52494646"), "image/webp"); // WebP（前4字节RIFF，后续字节辅助验证，简化匹配）

        // 文档格式
        MAGIC_NUMBER_TO_MIME.put(hexToBytes("25504446"), "application/pdf"); // PDF
        MAGIC_NUMBER_TO_MIME.put(hexToBytes("504B0304"), "application/zip"); // ZIP（含DOCX、XLSX等压缩包格式）
        MAGIC_NUMBER_TO_MIME.put(hexToBytes("7B5C727466"), "application/msword");// DOC（早期Word格式）

        // 压缩包/归档格式
        MAGIC_NUMBER_TO_MIME.put(hexToBytes("1F8B"), "application/gzip"); // GZIP（.tar.gz
                                                                          // 本质是gzip压缩）
        MAGIC_NUMBER_TO_MIME.put(hexToBytes("7573746172"), "application/x-tar");// TAR（关键字节ustar）

        // 文本格式（纯文本无明确魔数，通过“无其他魔数匹配”兜底）
        // 其他格式可按需扩展：如MP4(0000001866747970)、AVI(52494646)等
    }

    private static int getMMLen() {
        int n = 0;
        for (byte[] key : MAGIC_NUMBER_TO_MIME.keySet()) {
            n = Math.max(n, key.length);
        }
        return n;
    }

    // 2. 读取InputStream头部字节的最大长度（覆盖映射表中最长的魔数，这里最长应该是8字节）
    private static final int MAX_MAGIC_LENGTH = getMMLen();

    public static String getMimeType(File f) throws IOException {
        InputStream ins = Streams.fileIn(f);
        try {
            return getMimeType(ins);
        }
        finally {
            Streams.safeClose(ins);
        }
    }

    /**
     * 核心方法：传入InputStream，返回对应的MIME类型。
     * <p>
     * 如果这个流支持 mark/reset 本函数就没有任何副作用。 即你调用了本函数，还可以用其他的读取函数，就彷佛本函数没有被调用过一样
     * 
     * @param ins
     *            待检测的输入流，
     * 
     * @return MIME字符串（如image/jpeg，无匹配时返回text/plain兜底）
     * @throws IOException
     *             流读取异常
     */
    public static String getMimeType(InputStream ins) throws IOException {
        // 关键：标记流位置（后续要重置，避免影响调用方使用流）
        boolean supportMark = ins.markSupported();
        // 标记：读取MAX_MAGIC_LENGTH字节后，能重置回起始位置
        if (supportMark) {
            ins.mark(MAX_MAGIC_LENGTH);
        }

        try {
            // 读取头部MAX_MAGIC_LENGTH字节（不足时按实际长度读取）
            byte[] headBytes = new byte[MAX_MAGIC_LENGTH];
            int len = ins.read(headBytes);
            String matchedMime = getMimeType(headBytes, len);

            return matchedMime;

        }
        finally {
            // 必须重置流：回到mark的位置，确保调用方后续能正常读取整个流
            if (supportMark) {
                ins.reset();
            }
        }
    }

    public static String getMimeType(byte[] headBytes, int len) {
        // 截取实际读取到的字节（避免未读满时的空字节干扰匹配）
        byte[] actualHeader = Arrays.copyOf(headBytes, len);

        // 遍历魔数映射表，匹配最长可能的魔数（避免短魔数误判，如GIF的474946不匹配ZIP的504B）
        String matchedMime = "text/plain"; // 默认兜底：纯文本（无明确魔数时）
        int maxMatchedLength = 0; // 记录匹配到的魔数长度（优先最长匹配）

        for (Map.Entry<byte[], String> entry : MAGIC_NUMBER_TO_MIME.entrySet()) {
            byte[] magicBytes = entry.getKey();
            String mime = entry.getValue();

            // 条件：实际头部长度 >= 魔数长度，且头部前N字节与魔数完全一致
            if (actualHeader.length >= magicBytes.length
                && Arrays.equals(Arrays.copyOf(actualHeader, magicBytes.length), magicBytes)) {
                // 优先选择更长的魔数（比如PNG的8字节比WebP的4字节更精准）
                if (magicBytes.length > maxMatchedLength) {
                    maxMatchedLength = magicBytes.length;
                    matchedMime = mime;
                }
            }
        }
        return matchedMime;
    }

    /**
     * 辅助工具：将十六进制字符串转为字节数组（如"FFD8FF" → [0xFF, 0xD8, 0xFF]）
     * 
     * @param hexStr
     *            十六进制字符串（无需空格分隔）
     * @return 对应的字节数组
     */
    private static byte[] hexToBytes(String hexStr) {
        int len = hexStr.length();
        byte[] bytes = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            // 每2个十六进制字符对应1个字节
            bytes[i / 2] = (byte) ((Character.digit(hexStr.charAt(i), 16) << 4)
                                   + Character.digit(hexStr.charAt(i + 1), 16));
        }
        return bytes;
    }
}