package com.site0.walnut.ext.net.http.upload;

import java.io.IOException;
import java.io.InputStream;

import org.nutz.lang.Encoding;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;

/**
 * 封装了一个上传流的解析
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class HttpFormUpload {

    private static final String NWLN = "\r\n";

    private static final String NWLN2 = NWLN + NWLN;

    private static final byte[] NWLNBytes = NWLN.getBytes();

    private static final byte[] NWLN2Bytes = NWLN2.getBytes();

    private InputStream ins;

    private String boundary;

    /**
     * 首轮读取的边界
     * 
     * <pre>
     * "--" + boundary
     * </pre>
     */
    private byte[] boundBytes;

    /**
     * 缓冲为 boundary 的整数倍，否则会出问题
     */
    private byte[] buffer;

    /**
     * 副缓冲，每次读取完一个字段，将主缓冲剩余内容 copy 到这里 然后切换未主缓冲
     */
    private byte[] buffer2;

    /**
     * 缓冲大小的线索，为最接近这个尺寸的 boundary 大小整数倍
     */
    private int capacity;

    /**
     * 缓冲的有效字节数，加载缓冲时也会从这个位置开始写
     */
    private int limit;

    /**
     * 读取缓冲的起始位置，每次搜索 boundary 都从这个位置开始
     */
    private int rIndex;

    /**
     * 读字段内容，当读到边界时，标记一下。这样，当 rIndex 达到这个位置后就不再读取了。
     */
    private int boundIndex;

    /**
     * 当 findIndex 未圆满时，记录到底停留在子串哪个下标下
     */
    private int subFindIndex;

    /**
     * 表示 boundIndex 其实不是真的边界。 是为了 readToBound 是方便填充才设置的值。<br>
     * 因此当 rIndex 越过的时候，要试图再次加载缓冲，并重置本属性
     */
    private boolean boundIsNotBound;

    /**
     * 主要用来设置上传流每个项目的边界符。 可以接受下面三种格式：
     * <ul>
     * <li><code>"multipart/form-data; boundary=----xxx",</code>
     * <li><code>"boundary=----xxx",</code>
     * <li><code>"----xxx",</code>
     * </ul>
     * 
     * 总之就是为了寻找到一个边界符（不包括前后换行符）
     * 
     * @param ins
     *            输入流
     * 
     * @param bound
     *            边界字符串
     * @param capacity
     *            内部缓冲块大小（Byte），不能太小，必须能包括一个字段头
     */
    public HttpFormUpload(InputStream ins, String bound, int capacity) {
        this.ins = ins;
        this.capacity = capacity;
        this.setBoundary(bound);
        this.prepareBuffer();
    }

    public HttpFormUpload(InputStream ins, String bound) {
        this(ins, bound, 1024 * 1024);
    }

    public String getBoundary() {
        return boundary;
    }

    public void setBoundary(String bound) {
        String[] ss = Strings.splitIgnoreBlank(bound, ";");
        // 看看是否是 "boundary=----xxx" 的形式
        if (1 == ss.length) {
            if (ss[0].startsWith("boundary=")) {
                this.boundary = ss[0].substring("boundary=".length());
            } else {
                this.boundary = ss[0];
            }
        }
        // 逐个寻找
        else {
            for (String s : ss) {
                if (s.startsWith("boundary=")) {
                    this.boundary = s.substring("boundary=".length());
                    break;
                }
            }
        }
    }

    /**
     * Buffer 应该为 boundary 的整数倍，否则会尴尬
     */
    private void prepareBuffer() {
        String boundstr = "--" + this.boundary;
        this.boundBytes = boundstr.getBytes();
        this.buffer = new byte[this.capacity];
        this.buffer2 = new byte[this.capacity];
        this.rIndex = 0;
        this.limit = 0;

        // 重置标志
        this.boundIndex = -1;
        this.boundIsNotBound = false;
        this.subFindIndex = -1;
    }

    /**
     * 从缓冲里尝试搜索边界。它有三种情况
     * 
     * <ul>
     * <li><code>-1</code>未找到
     * <li><code>-2</code>未圆满。会记录匹配到子串的第几个下标
     * <li><code>>=0</code>找到, 那么这个值就是子串的起始下标
     * </ul>
     * 
     * @param bounds
     *            边界
     * @return 边界的开始位置。 -1 表示没有找到子串; -2 表示子串未匹配完
     */
    private int __find_index(byte[] bounds) {
        // 逐个搜索
        for (int i = rIndex; i <= limit; i++) {
            boolean match = true;
            // 匹配子串
            for (int x = 0; x < bounds.length; x++) {
                int iX = i + x;
                // 超过了主串长度，则没有匹配完
                if (iX >= limit) {
                    this.subFindIndex = x;
                    return -2;
                }

                // 如果有不匹配，则退出子串匹配，主串下移一个字符
                if (buffer[i + x] != bounds[x]) {
                    // 好像这样可以的，可以加速
                    // KMP 的索引表就不用建了，因为哪个字符失配
                    // 子串的回退位置都应该是 0
                    i = i + x;
                    match = false;
                    break;
                }
            }
            // 匹配了就返回下标
            if (match) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 从输入流加载一定的字节到自己的缓冲里
     * 
     * @return 实际加载的字节数
     *         <ul>
     *         <li><code>0</code> : 缓冲没有可写的地方了
     *         <li><code>-1</code> : 输入流木有内容了
     *         <li><code>>0</code> : 实际加载的字节数
     *         </ul>
     * @throws IOException
     */
    private int __load_buffer() throws IOException {
        // 缓冲写不下了
        if (limit >= capacity) {
            return 0;
        }

        // 缓冲不再有内容了，那么从流里读一下
        int len = ins.read(buffer, limit, capacity - limit);
        // 再试图读一次
        if (len == 0) {
            len = ins.read(buffer, limit, capacity - limit);
        }
        if (len < 0) {
            // 如果还有内容没有读完，则返回 0 以便继续
            if (rIndex < limit) {
                return 0;
            }
            return -1;
        }
        limit += len;

        return len;
    }

    private void __switch_buffer() {
        // 看看要 copy 多少
        int len = limit - rIndex;

        // Copy 剩余
        if (len > 0) {
            System.arraycopy(buffer, rIndex, buffer2, 0, len);
        }

        // 切换
        byte[] b = buffer2;
        buffer2 = buffer;
        buffer = b;
        rIndex = 0;
        limit = len;

        // 重置标志
        this.boundIndex = -1;
        this.boundIsNotBound = false;
        this.subFindIndex = -1;
    }

    /**
     * 逐个字段的解析
     * 
     * @param callback
     *            回调
     * @throws IOException
     */
    public void parse(HttpFormCallback callback) throws IOException {
        // 首次加载缓冲
        this.__load_buffer();

        // 跳过第一个边界
        int re = this.__find_index(boundBytes);
        if (re < 0) {
            return;
        }
        rIndex = re + boundBytes.length;

        do {
            // 读头
            re = this.__find_index(NWLN2Bytes);
            if (re < 0)
                return;

            // 头长度过小，肯定是到达流的末尾了
            int hlen = re - this.rIndex;
            if (hlen < 15) {
                return;
            }

            // 分析头
            byte[] bfHead = new byte[hlen];
            System.arraycopy(buffer, rIndex, bfHead, 0, bfHead.length);
            String sfHead = new String(bfHead, Encoding.CHARSET_UTF8);
            HttpFormUploadField field = new HttpFormUploadField(this, sfHead);

            // 移动下标
            rIndex = re + NWLN2Bytes.length;

            // 看回调怎么处理
            callback.handle(field);

            // 处理完一个字段，则切换缓冲块，以便对齐头部
            __switch_buffer();

            // 然后试图加载一下数据
        } while (__load_buffer() >= 0);

    }

    /**
     * 一直读取到边界，如果未找到边界，则持续加载 buffer
     * 
     * @param bs
     *            要输出的缓冲
     * @param off
     *            开始写的位置
     * @param len
     *            写的长度
     * @return 实际写的长度。 -1 表示已经到边界了，不在有内容可以读取了
     * @throws IOException
     */
    public int readToBound(byte[] bs, int off, int len) throws IOException {
        // 已经找到过了边界 ...
        if (this.boundIndex >= 0) {
            // 读下标越界，那么这意味着 ...
            if (this.rIndex >= this.boundIndex) {
                // 界标非标，嗯，看来得加载一波
                if (this.boundIsNotBound) {
                    // 如果有未圆满的匹配，那么就是 -2导致的
                    // 先将这个小尾巴 copy 过去，然后再加载
                    // 此时的 rIndex 应该是 boundIndex
                    // 而 limit 前就是小尾巴，所以切换缓冲即可
                    // > 切换缓冲，也会导致全部界标相关标志位重置
                    __switch_buffer();

                    // 加载一下
                    if (this.__load_buffer() < 0) {
                        return -1;
                    }
                }
                // 就是真正的界标，不能再读了
                else {
                    return -1;
                }
            }
            // 填充吧
            else {
                return __fill_to_bound_and_update_rIndex(bs, off, len, !this.boundIsNotBound);
            }
        }

        // 首先试图寻找边界
        int index = this.__find_index(boundBytes);

        // 1. 找到边界了
        if (index >= rIndex) {
            this.boundIndex = index;
            int real = __fill_to_bound_and_update_rIndex(bs, off, len, true);
            return real;
        }
        // 2. 如果没有找到
        // 那就把缓冲剩余的内容统统 copy 过去
        // 并期待下次调用本函数，以便从输入流读取更多字节
        if (-1 == index) {
            this.boundIndex = this.limit;
            this.boundIsNotBound = true;
            return __fill_to_bound_and_update_rIndex(bs, off, len, false);
        }
        // 3. 没有匹配完成
        if (-2 == index) {
            this.boundIndex = this.limit - this.subFindIndex;
            this.boundIsNotBound = true;
            return __fill_to_bound_and_update_rIndex(bs, off, len, false);
        }
        // 没可能
        throw Lang.impossible();
    }

    private int __fill_to_bound_and_update_rIndex(byte[] bs, int off, int len, boolean foundBound) {
        // 剩下可以用来填充的长度
        int remain = this.boundIndex - this.rIndex;

        // 比较一下看看应该填充多少
        int real = Math.min(remain, len);

        // 因为找到了边界，内容要减去一个 NWLN
        if (foundBound && (real + rIndex) == boundIndex) {
            real -= NWLNBytes.length;
            System.arraycopy(buffer, rIndex, bs, off, real);
            rIndex = boundIndex;
        }
        // 输出缓冲不够大，直接填把
        else {
            System.arraycopy(buffer, rIndex, bs, off, real);
            rIndex += real;
        }

        // 返回实际填充的大小
        return real;
    }

    public void parseAndClose(HttpFormCallback callback) throws IOException {
        try {
            parse(callback);
        }
        finally {
            Streams.safeClose(ins);
        }
    }

    public NutMap parseDataAndClose() throws IOException {
        NutMap re = new NutMap();
        parseAndClose(new HttpFormCallback() {

            @Override
            public void handle(HttpFormUploadField field) throws IOException {
                String key = field.getName();
                if (field.isText()) {
                    String text = field.readAllString();
                    re.addv(key, text);
                }
                // 文件
                else if (field.isFile()) {
                    byte[] bs = field.readAllBytes();
                    HttpFormFile ff = new HttpFormFile(field, bs);
                    re.addv(key, ff);
                }
            }

        });

        return re;
    }

    public NutMap parseTextAndClose() throws IOException {
        NutMap re = new NutMap();
        parseAndClose(new HttpFormCallback() {

            @Override
            public void handle(HttpFormUploadField field) throws IOException {
                String key = field.getName();
                String text = field.readAllString();
                re.addv(key, text);
            }

        });

        return re;
    }

}