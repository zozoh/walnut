package com.site0.walnut.ext.httpapi;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.nutz.img.Images;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.ext.sys.httpapi.HttpApiDynamicRender;

public class HttpApiDynamicRenderTest {

    protected HttpServletResponse resp;
    protected ByteArrayOutputStream respOut;
    protected HttpApiDynamicRender render;
    protected OutputStream stdout;
    protected OutputStream stderr;
    protected NutMap answers;

    @Before
    public void before() throws IOException {
        respOut = new ByteArrayOutputStream();
        resp = mock(HttpServletResponse.class);
        ServletOutputStream out = new ServletOutputStream() {
            public void write(int b) throws IOException {
                respOut.write(b);
            }

            public void setWriteListener(WriteListener writeListener) {}

            public boolean isReady() {
                return true;
            }
        };
        when(resp.getOutputStream()).thenReturn(out);
        render = new HttpApiDynamicRender(resp);
        stdout = render.getStdout();
        stderr = render.getStderr();
        answers = new NutMap();
        // 模拟setStatus和getStatus
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) throws Throwable {
                answers.put("respStatus", invocation.getArgument(0));
                return null;
            }
        }).when(resp).setStatus(anyInt());
        when(resp.getStatus()).thenAnswer((invocation) -> answers.getInt("respStatus", 200));
        // 模拟setHeader
        doAnswer((invocation) -> {
            answers.put("http_header_" + invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(resp).setHeader(anyString(), anyString());
    }

    // 简单输出,没有HTTP/1.x
    @Test
    public void test_stdout_simple() throws IOException {
        stdPrint("ABC");
        render.close();
        assertEquals("ABC", respOut.toString());
    }

    // 先错误输出,然后标准输出
    @Test
    public void test_stderr_simple() throws IOException {
        errPrint("HTTP/1.1 200 OK\r\n");
        stdPrint("ABC");
        render.close();
        assertEquals("HTTP/1.1 200 OK\r\nABC", respOut.toString());
        assertEquals(500, answers.getInt("respStatus", 200));
    }

    // 标准输出,但只有头部,没有body
    @Test
    public void test_stdout_with_http() throws IOException {
        stdPrint("HTTP/1.1 400 OK\r\nNAME:wendal\r\nYYY:123\r\n\r\n");
        render.close();
        assertEquals(400, resp.getStatus());
        assertEquals("wendal", answers.get("http_header_NAME"));
        assertEquals("123", answers.get("http_header_YYY"));
    }

    // 标准输出,但只有头部,有body
    @Test
    public void test_stdout_with_http_and_body() throws IOException {
        stdPrint("HTTP/1.1 400 OK\r\nContent-Length:4\r\n\r\nFUCK");
        render.close();
        assertEquals(400, resp.getStatus());
        assertEquals("4", answers.get("http_header_Content-Length"));
        assertEquals("FUCK", respOut.toString());
    }

    // 标准输出,但只有头部,有body,且分段写入
    @Test
    public void test_stdout_with_http_and_body_part_by_part() throws IOException {
        stdPrint("HT");
        stdPrint("TP/1");
        stdPrint(".1 40");
        stdPrint("0 OK\r\nCont");
        stdPrint("ent-Len");
        stdPrint("gth:4\r\n\r\nF");
        stdPrint("");
        stdPrint("UCK");
        render.close();
        assertEquals(400, resp.getStatus());
        assertEquals("4", answers.get("http_header_Content-Length"));
        assertEquals("FUCK", respOut.toString());
    }

    // 试试图片数据
    @Test
    public void test_stdout_with_image() throws IOException {
        BufferedImage image = new BufferedImage(1920, 1080, BufferedImage.TYPE_3BYTE_BGR);
        image.createGraphics().drawString("ABC", 10, 10);
        Images.write(image, "jpg", stdout);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Images.write(image, "jpg", out);
        render.close();
        assertEquals(200, resp.getStatus());
        byte[] fromResp = respOut.toByteArray();
        byte[] fromReal = out.toByteArray();
        //org.nutz.lang.Files.write("D:\\real.jpg", fromReal);
        //org.nutz.lang.Files.write("D:\\resp.jpg", fromResp);
        assertEquals(fromReal.length, fromResp.length);
        for (int i = 0; i < fromReal.length; i++) {
            if (fromReal[i] != fromResp[i]) {
                System.out.println("INDEX =>"
                                   + i
                                   + ",real="
                                   + fromReal[i]
                                   + ",resp="
                                   + fromResp[i]);
                fail();
            }
        }
        assertTrue(Arrays.equals(out.toByteArray(), respOut.toByteArray()));
        image = Images.read(new ByteArrayInputStream(respOut.toByteArray()));
        assertNotNull(image);
        assertEquals(1920, image.getWidth());
        assertEquals(1080, image.getHeight());
    }

    // 非标准输出,只有\n\n,没有\r\n\r\n
    @Test
    public void test_stdout_not_standtor() throws IOException {
        stdPrint("HTTP/1.1 400 OK\nContent-Length:4\n\nFUCK");
        render.close();
        assertEquals(400, resp.getStatus());
        assertEquals("4", answers.get("http_header_Content-Length"));
        assertEquals("FUCK", respOut.toString());
    }

    // 非标准输出,只有\n\r\n,没有\r\n\r\n
    @Test
    public void test_stdout_not_standard2() throws IOException {
        stdPrint("HTTP/1.1 400 OK\nContent-Length:4\n\r\nFUCK");
        render.close();
        assertEquals(400, resp.getStatus());
        assertEquals("4", answers.get("http_header_Content-Length"));
        assertEquals("FUCK", respOut.toString());
    }

    // 非标准输出,只有\r\n\n,没有\r\n\r\n
    @Test
    public void test_stdout_not_standard3() throws IOException {
        stdPrint("HTTP/1.1 400 OK\nContent-Length:4\r\n\nFUCK");
        render.close();
        assertEquals(400, resp.getStatus());
        assertEquals("4", answers.get("http_header_Content-Length"));
        assertEquals("FUCK", respOut.toString());
    }

    // 非标准输出,快到分隔符的地方,分段输出
    @Test
    public void test_stdout_not_standard_part_by_part() throws IOException {
        stdPrint("HTTP/1.1 400 OK\n");
        stdPrint("Content-Length:4\r\n");
        stdPrint("");
        stdPrint("\nFU");
        stdPrint("");
        stdPrint("CK");
        render.close();
        assertEquals(400, resp.getStatus());
        assertEquals("4", answers.get("http_header_Content-Length"));
        assertEquals("FUCK", respOut.toString());
    }

    protected void stdPrint(String str) throws IOException {
        stdout.write(str.getBytes());
    }

    protected void errPrint(String str) throws IOException {
        stderr.write(str.getBytes());
    }
}
