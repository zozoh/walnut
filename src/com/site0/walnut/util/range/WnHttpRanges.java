package com.site0.walnut.util.range;

import com.site0.walnut.util.Ws;

/**
 * HTTP 条件请求 / 区间请求的<b>纯计算</b>：不碰 IO，不碰响应对象。
 *
 * <p>把 #494 需要的行为固化在一处：</p>
 * <ul>
 * <li>ETag 带引号输出（RFC 9110 的 entity-tag 语法）；</li>
 * <li><code>If-None-Match</code> 比较（容忍 <code>W/</code> 前缀、逗号分隔多值、<code>*</code>）；</li>
 * <li>单区间 <code>Range</code> 解析：<code>bytes=a-b</code> / <code>a-</code> / <code>-n</code>，
 * 上界夹到内容长度；</li>
 * <li>不可满足的区间 → <code>416</code> + <code>Content-Range: bytes * / len</code>。</li>
 * </ul>
 *
 * <p>多区间（<code>bytes=0-1,5-9</code>）与语法坏的 Range 一律忽略，按 <code>200</code>
 * 全量处理——不做 <code>multipart/byteranges</code>（RFC 允许服务端忽略 Range）。</p>
 *
 * <p>无状态，线程安全。</p>
 */
public final class WnHttpRanges {

    /** 200：整文件。 */
    public static final int SC_OK = 200;

    /** 206：部分内容。 */
    public static final int SC_PARTIAL = 206;

    /** 304：未修改。 */
    public static final int SC_NOT_MODIFIED = 304;

    /** 416：区间不可满足。 */
    public static final int SC_RANGE_NOT_SATISFIABLE = 416;

    private WnHttpRanges() {}

    /**
     * 一次判定的结果。
     */
    public static class Decision {

        /** HTTP 状态码：200 / 206 / 304 / 416。 */
        public int status = SC_OK;

        /** 要读的起始偏移（含）。 */
        public long start;

        /** 要读的结束偏移（不含）。 */
        public long end;

        /** 要发送的字节数（200 / 206 有效；304 / 416 为 0）。 */
        public long length;

        /** <code>Content-Range</code> 头（206 / 416 有值，其余为 <code>null</code>）。 */
        public String contentRange;
    }

    /**
     * 把 ETag 变成合法的 entity-tag：外面套引号。
     *
     * <p>已经是带引号形式时原样返回；<code>null</code>/空串返回 <code>null</code>。</p>
     */
    public static String quote(String etag) {
        if (Ws.isBlank(etag))
            return null;
        String s = etag.trim();
        if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\""))
            return s;
        return "\"" + s + "\"";
    }

    /**
     * <code>If-None-Match</code> 是否命中给定 ETag。
     *
     * @param etag
     *            资源当前 ETag（不带引号也可）
     * @param ifNoneMatch
     *            请求头原文，可为 <code>null</code>
     * @return <code>true</code> 表示命中（应回 304）
     */
    public static boolean noneMatch(String etag, String ifNoneMatch) {
        if (Ws.isBlank(etag) || Ws.isBlank(ifNoneMatch))
            return false;
        String tag = unquote(etag);
        for (String part : ifNoneMatch.split(",")) {
            String s = part.trim();
            if (s.isEmpty())
                continue;
            if ("*".equals(s))
                return true;
            if (s.startsWith("W/"))
                s = s.substring(2).trim();
            if (tag.equals(unquote(s)))
                return true;
        }
        return false;
    }

    /**
     * 综合判定一次静态资源响应。
     *
     * @param len
     *            资源总字节数
     * @param etag
     *            资源 ETag（不带引号）
     * @param ifNoneMatch
     *            请求头 <code>If-None-Match</code>
     * @param range
     *            请求头 <code>Range</code>
     * @return 判定结果，调用方按 {@link Decision#status} 落状态码与头
     */
    public static Decision decide(long len, String etag, String ifNoneMatch, String range) {
        Decision d = new Decision();

        // 1) 条件请求：命中即 304（不带 Content-Length）
        if (noneMatch(etag, ifNoneMatch)) {
            d.status = SC_NOT_MODIFIED;
            return d;
        }

        // 2) 无 Range：整文件
        if (Ws.isBlank(range)) {
            full(d, len);
            return d;
        }

        // 3) 空文件 + Range：区间必然不可满足
        if (len <= 0) {
            unsatisfiable(d, len);
            return d;
        }

        // 4) 解析单区间；多区间 / 语法坏 → 忽略 Range 回全量
        String r = range.trim();
        if (!r.startsWith("bytes=")) {
            full(d, len);
            return d;
        }
        String spec = r.substring("bytes=".length()).trim();
        if (spec.isEmpty() || spec.indexOf(',') >= 0) {
            full(d, len);
            return d;
        }
        int p = spec.indexOf('-');
        if (p < 0) {
            full(d, len);
            return d;
        }
        String a = spec.substring(0, p).trim();
        String b = spec.substring(p + 1).trim();
        try {
            long start;
            long end;
            // bytes=-n：最后 n 个字节
            if (a.isEmpty()) {
                long suffix = Long.parseLong(b);
                if (suffix <= 0) {
                    unsatisfiable(d, len);
                    return d;
                }
                start = Math.max(0, len - suffix);
                end = len;
            }
            // bytes=a- / bytes=a-b
            else {
                start = Long.parseLong(a);
                if (start < 0 || start >= len) {
                    unsatisfiable(d, len);
                    return d;
                }
                end = b.isEmpty() ? len : Math.min(len, Long.parseLong(b) + 1);
                if (end <= start) {
                    unsatisfiable(d, len);
                    return d;
                }
            }
            d.status = SC_PARTIAL;
            d.start = start;
            d.end = end;
            d.length = end - start;
            d.contentRange = String.format("bytes %d-%d/%d", start, end - 1, len);
            return d;
        }
        catch (NumberFormatException e) {
            // 数字都解析不了，当没有 Range
            full(d, len);
            return d;
        }
    }

    // ----------------------------------------------------------------------
    // 内部
    // ----------------------------------------------------------------------

    private static void full(Decision d, long len) {
        d.status = SC_OK;
        d.start = 0;
        d.end = len;
        d.length = len;
        d.contentRange = null;
    }

    private static void unsatisfiable(Decision d, long len) {
        d.status = SC_RANGE_NOT_SATISFIABLE;
        d.start = 0;
        d.end = 0;
        d.length = 0;
        d.contentRange = "bytes */" + len;
    }

    private static String unquote(String s) {
        String v = null == s ? "" : s.trim();
        if (v.length() >= 2 && v.startsWith("\"") && v.endsWith("\""))
            v = v.substring(1, v.length() - 1);
        return v;
    }

}
