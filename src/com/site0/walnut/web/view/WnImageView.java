package com.site0.walnut.web.view;

import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.site0.walnut.util.Wlang;
import org.nutz.lang.Streams;
import org.nutz.lang.util.ByteInputStream;
import org.nutz.mvc.View;

public class WnImageView implements View {

    private static byte[] dft_img;

    static {
        dft_img = Streams.readBytesAndClose(WnImageView.class.getClassLoader()
                                                             .getResourceAsStream("com/site0/walnut/web/view/unknown_image_type.jpg"));
    }

    private String type;

    private String mime;

    public WnImageView(String type, String mime) {
        this.type = type;
        this.mime = mime;
    }

    @Override
    public void render(HttpServletRequest req, HttpServletResponse resp, Object obj)
            throws Exception {
        byte[] buf = dft_img;
        try {
            // 如果是个图片
            if (obj instanceof RenderedImage) {
                // 写入响应体
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                RenderedImage im = (RenderedImage) obj;
                ImageIO.write(im, type, output);
                buf = output.toByteArray();
            }
            // 如果是一个输入流
            else if (obj instanceof InputStream) {
                InputStream ins = (InputStream) obj;
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                Streams.writeAndClose(output, ins);
                buf = output.toByteArray();
            }
            // 根本就是一个字节数组
            else if (obj instanceof byte[]) {
                buf = (byte[]) obj;
            }
        }
        catch (Exception e) {
            throw e;
        }
        // 标记响应头
        String _etag = req.getHeader("If-None-Match");
        String etag = Wlang.sha1(new ByteInputStream(buf)).substring(0, 12);
        if (etag.equals(_etag)) {
            resp.setStatus(304);
            return;
        }
        resp.setContentType(mime);
        resp.setHeader("ETag", etag);
        resp.setContentLength(buf.length);
        resp.getOutputStream().write(buf);
    }

}
