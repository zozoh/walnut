package org.nutz.walnut.util;

import java.io.InputStream;
import java.io.OutputStream;

import org.nutz.lang.Lang;
import org.nutz.walnut.api.box.WnTurnnel;
import org.nutz.walnut.api.err.Er;

/**
 * 本类的使用场景假想的是，一块中间的数据缓冲，一个线程读，一个线程写。
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class JvmTurnnel implements WnTurnnel {

    JTRow r4W;

    JTRow r4R;

    /**
     * 一共读过多少字节
     */
    private long rsum;

    private int rowWidth;

    private JTInputStream _ins;

    private JTOutputStream _ops;

    public JvmTurnnel(int width) {
        r4W = new JTRow(null, width);
        r4R = r4W;
        rowWidth = width;
        rsum = 0;
        _ins = new JTInputStream(this);
        _ops = new JTOutputStream(this);
    }

    @Override
    public InputStream asInputStream() {
        return _ins;
    }

    @Override
    public OutputStream asOutputStream() {
        return _ops;
    }

    @Override
    public synchronized void close() {
        r4W = null;
        r4R = null;
    }

    @Override
    public synchronized byte read() {
        if (null == r4R)
            throw Er.create("e.box.jvm.close.read");

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
    public synchronized int read(byte[] bs, int off, int len) {
        if (null == r4R)
            throw Er.create("e.box.jvm.close.read");

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
    public synchronized void write(byte b) {
        if (null == r4W)
            throw Er.create("e.box.jvm.close.write");

        if (r4W.write(b))
            return;

        r4W = new JTRow(r4W, rowWidth);
        if (!r4W.write(b))
            throw Lang.impossible();
    }

    @Override
    public synchronized void write(byte[] bs, int off, int len) {
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

    static class JTRow {

        byte[] bytes;

        JTRow next;

        int ir;

        int iw;

        int remain;

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
