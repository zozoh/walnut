package org.nutz.walnut.util.upload;

import java.io.IOException;
import java.io.InputStream;

import org.nutz.lang.util.LinkedByteBuffer;

public class HttpFormField extends FormField {

    private HttpFormUpload upload;

    /**
     * 读完了的标志位
     */
    private boolean done;

    public HttpFormField(HttpFormUpload upload) {
        this.upload = upload;
    }

    public HttpFormField(HttpFormUpload upload, String str) {
        super(str);
        this.upload = upload;
    }

    public InputStream getInputStream() {
        return new HttpFormFieldInputStream(this);
    }

    public LinkedByteBuffer readAll() throws IOException {
        LinkedByteBuffer lbb = new LinkedByteBuffer();
        byte[] buf = new byte[8192];
        int len = 0;
        while ((len = read(buf)) >= 0) {
            lbb.write(buf, 0, len);
        }
        return lbb;
    }

    public String readAllString() throws IOException {
        LinkedByteBuffer lbb = this.readAll();
        return lbb.toString();
    }

    public byte[] readAllBytes() throws IOException {
        LinkedByteBuffer lbb = this.readAll();
        return lbb.toArray();
    }

    public int read(byte[] bs) throws IOException {
        return read(bs, 0, bs.length);
    }

    public int read(byte[] bs, int off, int len) throws IOException {
        // 读完了就不在读了
        if (done)
            return -1;

        // 读一下
        int re = upload.readToBound(bs, off, len);

        // 如果已经读到边界了，标记一下
        if (re < 0) {
            done = true;
            return -1;
        }

        // 返回读取的实际内容长度
        return re;
    }

}
