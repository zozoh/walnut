package com.site0.walnut.ext.sys.httpapi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import com.site0.walnut.util.Wlang;
import org.nutz.lang.Strings;

/**
 * 封装HttpAPI处理动态头部时的逻辑
 * 
 * @author wendal
 *
 */
public class HttpApiDynamicRender {

    // 保存resp对象,随时可用嘛
    protected HttpServletResponse resp;

    // 0 - 什么都还没发生
    // 1 - 标准输出流有输入,正在处理
    // 2 - 标准错误输入流有输入,正在处理
    // 3 - 已经检查过头部了,的确是HTTP/1.1开头,需要更多数据
    // 5 - 不要再检测了,直接写流吧
    protected int status;

    /**
     * 缓存stdout的输入,用于检测HTTP/1.1及header的结尾
     */
    protected ByteArrayOutputStream stdoutBuf;
    /**
     * 记录上一次检测HTTP/1.1及header的结尾的位置
     */
    protected int lastOffset;
    /**
     * Resp的输出流
     */
    protected OutputStream out;
    private static final Pattern _P = Pattern.compile("^HTTP/1.\\d\\s+(\\d+)(\\s+(.*))?$");

    public HttpApiDynamicRender(HttpServletResponse resp) throws IOException {
        this.resp = resp;
        this.out = resp.getOutputStream();
    }

    /**
     * 返回一个虚拟的标准输出流
     */
    public OutputStream getStdout() {
        return new OutputStream() {
            public void write(int b) throws IOException {
                // 统统交给stdOut处理
                stdOut(new byte[]{(byte) b}, 0, 1);
            }

            public void write(byte[] b, int off, int len) throws IOException {
                // 统统交给stdOut处理
                stdOut(b, off, len);
            }
        };
    }

    /**
     * 返回一个虚拟的标准错误流
     */
    public OutputStream getStderr() {
        return new OutputStream() {
            public void write(int b) throws IOException {
                // 统统交给errOut处理
                errOut(new byte[]{(byte) b}, 0, 1);
            }

            public void write(byte[] b, int off, int len) throws IOException {
                // 统统交给errOut处理
                errOut(b, off, len);
            }
        };
    }

    public void errOut(byte[] buf, int off, int len) throws IOException {
        // 如果还在初始状态
        if (status == 0) {
            if (resp.isCommitted()) {
                // 已经commit了? 那就不能改status了!
            } else {
                // 来吧,reset the world, 设置为500
                resp.reset();
                resp.setStatus(500);
            }
            status = 2;
        }
        // 管他呢,写写写
        out.write(buf, off, len);
    }

    public void stdOut(byte[] buf, int off, int len) throws IOException {
        // 还是初始状态吗? 那就是标准输入先写入数据的
        if (status == 0) {
            status = 1;
        }
        // 错误流先写入数据了
        else if (status == 2) {
            out.write(buf, off, len);
            return;
        }
        // 已过了检测阶段?直接写流
        else if (status == 5) {
            out.write(buf, off, len);
            return;
        }
        // 看来要准备stdout的缓冲了
        if (stdoutBuf == null) {
            stdoutBuf = new ByteArrayOutputStream();
        }
        stdoutBuf.write(buf, off, len);
        // 标准输入先来的,数据足够的话,检测HTTP/1.x咯
        if (status == 1 && stdoutBuf.size() > 10) {
            try {
                String tmp = new String(stdoutBuf.toByteArray(), 0, 10);
                if (tmp.startsWith("HTTP/1.")) {
                    status = 3; // 需要进一步检测header的尾部
                } else {
                    status = 5; // 不是头部,再见
                }
            }
            catch (Throwable e) {
                status = 5; // 不是文本? 再见
            }
            if (status != 3) {
                // 呵呵,不是头部,再见
                out.write(stdoutBuf.toByteArray());
                stdoutBuf = null;
                status = 5; // 呵呵,再见...
                return;
            }
        }
        if (status == 3) {
            // 看看头部结束没
            checkHttpHeader();
        }
    }

    protected void checkHttpHeader() throws IOException {
        byte[] tmp = stdoutBuf.toByteArray();
        for (int i = lastOffset; i < tmp.length - 1; i++) {
            // 标准的header总是以\r\n\r\n结束
            int endIndex = 0;
            if (tmp[i] == '\n') {
                if (tmp[i + 1] == '\r') {
                    if (tmp.length - i < 2) {
                        // 越界了, 等下一个数据吧
                        return;
                    } else if (tmp[i + 2] == '\n') {
                        endIndex = 2;
                    }
                } else if (tmp[i + 1] == '\n') {
                    endIndex = 1;
                }
            }
            if (endIndex > 0) {
                // 找到啦!!!
                String str = new String(tmp, 0, i + endIndex);
                // 处理之
                int pos = str.indexOf('\n');
                // 读取返回码
                String sStatus = str.substring(0, pos);
                Matcher m = _P.matcher(sStatus);
                if (!m.find())
                    throw Wlang.makeThrow("invalid HTTP status line: %s", sStatus);

                int statusCode = Integer.parseInt(m.group(1));
                // zozoh: 下面貌似木有用，先注释掉
                // String statusText = Strings.trim(m.group(3));
                // if (Strings.isBlank(statusText))
                // statusText = Http.getStatusText(statusCode);
                resp.setStatus(statusCode);
                // 读取头部信息
                pos++;
                int end;
                while ((end = str.indexOf('\n', pos)) > pos) {
                    String line = str.substring(pos, end);
                    // 拆分一下行
                    int p2 = line.indexOf(':');
                    String key = Strings.trim(line.substring(0, p2));
                    String val = Strings.trim(line.substring(p2 + 1));
                    resp.setHeader(key, val);
                    // 指向下一行
                    pos = end + 1;
                }

                // 头部解析完成, 剩余的字节写入resp
                if (i < tmp.length - endIndex - 1) {
                    out.write(tmp, i + endIndex + 1, tmp.length - i - endIndex - 1);
                }
                stdoutBuf = null;
                status = 5;
                break;
            }
        }
        lastOffset = tmp.length - 2;
        if (lastOffset > 8196) { // 读了8k还找不到?再见了
            out.write(tmp);
            stdoutBuf = null;
            status = 5;
        }
    }

    public void close() throws IOException {
        // 关闭的时候看看有没有缓冲流, 应该是没有的,这里只是防御一下
        if (stdoutBuf != null) {
            byte[] buf = stdoutBuf.toByteArray();
            out.write(buf);
        }
    }
}
