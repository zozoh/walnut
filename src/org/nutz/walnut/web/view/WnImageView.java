package org.nutz.walnut.web.view;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.mvc.View;

public class WnImageView implements View {

    private static BufferedImage dft_img;

    {
        try {
            InputStream ins = Streams.fileIn("org/nutz/walnut/web/view/unknown_image_type.jpg");
            try {
                dft_img = ImageIO.read(ins);
            }
            finally {
                Streams.safeClose(ins);
            }
        }
        catch (IOException e) {
            throw Lang.wrapThrow(e);
        }
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
        OutputStream output = resp.getOutputStream();
        try {
            // 如果是个图片
            if (obj instanceof RenderedImage) {
                // 标记响应头
                resp.setContentType(mime);
                // 写入响应体
                RenderedImage im = (RenderedImage) obj;
                ImageIO.write(im, type, output);
            }
            // 否则就用默认的
            else {
                // 标记响应头
                resp.setContentType(mime);
                // 写入响应体
                ImageIO.write(dft_img, "image/jpeg", output);
            }
        }
        catch (Exception e) {
            throw e;
        }
        finally {
            resp.flushBuffer();
            Streams.safeClose(output);
        }
    }

}
