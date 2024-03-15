package com.site0.walnut.util;

import java.io.InputStream;
import java.io.OutputStream;

import com.site0.walnut.api.box.WnTunnel;
import com.site0.walnut.api.err.Er;

/**
 * 本类的使用场景假想的是，一块中间的数据缓冲，一个线程读，一个线程写。
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class JvmTunnel implements WnTunnel {

    JTRow r4W;

    JTRow r4R;

    /**
     * 一共读过多少字节
     */
    private long rsum;

    private int rowWidth;

    private JTInputStream _ins;

    private JTOutputStream _ops;

    public JvmTunnel(int width) {
        rowWidth = width;
        reset();
    }

    @Override
    public void reset() {
        r4W = new JTRow(null, rowWidth);
        r4R = r4W;
        rsum = 0;
    }

    public long size() {
        long re = 0;
        JTRow r = r4R;
        while (null != r) {
            re += r.iw - r.ir;
            r = r.next;
        }
        return re;
    }

    @Override
    public InputStream asInputStream() {
        if (null == _ins) {
            _ins = new JTInputStream(this);
        }
        return _ins;
    }

    @Override
    public OutputStream asOutputStream() {
        if (null == _ops) {
            _ops = new JTOutputStream(this);
        }
        return _ops;
    }

    @Override
    public void close() {
        r4W = null;
        r4R = null;
    }

    @Override
    public void closeRead() {
        r4R = null;
    }

    @Override
    public void closeWrite() {
        r4W = null;
    }

    @Override
    public boolean isReadable() {
        return null != r4R;
    }

    @Override
    public boolean isWritable() {
        return null != r4W;
    }

    @Override
    public byte read() {
        if (null == r4R)
            return -1;

        byte re = r4R.read();
        // 读到了
        if (re != -1) {
            rsum++;
            return re;
        }

        // 下移一行读取
        if (r4R.next == null)
            return -1;

        r4R = r4R.next;
        re = r4R.read();
        // 读到了
        if (re != -1) {
            rsum++;
        }
        return re;
    }

    @Override
    public int read(byte[] bs) {
        return read(bs, 0, bs.length);
    }

    @Override
    public int read(byte[] bs, int off, int len) {
        if (null == r4R)
            return -1;

        if (len <= 0 || off >= bs.length)
            return 0;

        long sum = rsum;
        int re;
        while (true) {
            re = r4R.read(bs, off, len);
            // 空了，移动到下一行继续读取
            if (re == -1) {
                // 没下一行就
                if (r4R.next == null)
                    break;
                r4R = r4R.next;
                continue;
            }
            // 如果读到的字节符合要求
            if (re == len) {
                rsum += re;
                break;
            }
            // 否则读到的字符小于期望读取的字符，那么进入下一行
            rsum += re;

            // 没下一行
            if (r4R.next == null)
                break;
            r4R = r4R.next;
            off += re; // 目标从已读取的字节后追加
            len -= re; // 读取的长度减去已经读取的字节数
            continue;
        }

        // 返回读取的字节数，啥都没读到，则表示流结束了
        long c = rsum - sum;
        if (0 == c)
            return -1;
        return (int) c;
    }

    @Override
    public void write(byte b) {
        if (null == r4W)
            throw Er.create("e.box.jvm.close.write");

        if (r4W.write(b))
            return;

        r4W = new JTRow(r4W, rowWidth);
        if (!r4W.write(b))
            throw Wlang.impossible();
    }

    @Override
    public void write(byte[] bs, int off, int len) {
        if (null == r4W)
            throw Er.create("e.box.jvm.close.write");

        int n = r4W.write(bs, off, len);
        while (n > 0) {
            r4W = new JTRow(r4W, rowWidth);
            off = off + len - n;
            len = n;
            n = r4W.write(bs, off, len);
        }
    }

    /**
     * @see #write(byte[], int, int)
     */
    @Override
    public void write(byte[] bs) {
        write(bs, 0, bs.length);
    }

    /**
     * @return 一共读取过多少字节
     */
    @Override
    public long getReadSum() {
        return rsum;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("r4W:%s   r4R:%s\n", r4W != null, r4R != null));
        JTRow r = r4R;
        while (null != r) {
            sb.append(r.toString()).append("\n");
            r = r.next;
        }
        return sb.toString();
    }

    static class JTRow {

        byte[] bytes;

        JTRow next;

        int ir;

        int iw;

        int remain;

        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sb.append('[');
                if (ir == i) {
                    sb.append(">>");
                }
                switch (bytes[i]) {
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    sb.append((char) bytes[i]);
                }
                if (iw == i) {
                    sb.append("<<");
                }
                sb.append(']');
            }
            return sb.toString();
        }

        JTRow(JTRow prev, int width) {
            bytes = new byte[width];
            if (null != prev)
                prev.next = this;
            ir = 0;
            iw = 0;
            remain = width;
        }

        byte read() {
            if (ir < iw)
                return bytes[ir++];
            return -1;
        }

        int read(byte[] bs, int off, int len) {
            if (ir < iw) {
                int n = iw - ir; // 可读长度
                // 肯定能读完
                if (n >= len) {
                    System.arraycopy(bytes, ir, bs, off, len);
                    ir += len;
                    return len;
                }
                // 只能读出一部分
                System.arraycopy(bytes, ir, bs, off, n);
                ir = iw;
                return n;
            }
            return -1;
        }

        /**
         * @param b
         *            字节
         * @return 是否成功写入
         */
        boolean write(byte b) {
            if (remain <= 0)
                return false;
            bytes[iw++] = b;
            remain--;
            return true;
        }

        /**
         * @param bs
         *            字节数组
         * @param off
         *            从哪里开始写
         * @param len
         *            写多少
         * @return 0 表示都消费完了， >0 表示还多少字节写不下了
         */
        int write(byte[] bs, int off, int len) {
            if (remain <= 0 || len <= 0 || off >= bs.length)
                return len;
            // 肯定写的下
            if (remain >= len) {
                System.arraycopy(bs, off, bytes, iw, len);
                remain -= len;
                iw += len;
                return 0;
            }
            // 只能写一部分
            int n = len - remain;
            System.arraycopy(bs, off, bytes, iw, remain);
            remain = 0;
            iw = bytes.length;
            return n;

        }
    }

}
