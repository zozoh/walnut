package org.nutz.walnut.ext.media.timg.builder;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.nutz.img.Images;
import org.nutz.log.Log;
import org.nutz.walnut.util.Wlog;
import org.nutz.walnut.ext.media.timg.AbstractCartonBuilder;
import org.nutz.walnut.ext.media.timg.CartonCtx;

public class FadeCartonBuilder extends AbstractCartonBuilder {

    protected static final Log log = Wlog.getCMD();

    @Override
    public void _invoke(CartonCtx ctx) {
        int cartonFrameCount = ctx.cur.cartonTime / (1000 / ctx.fps);
        log.infof("转场共%d帧", cartonFrameCount);
        int w = ctx.w;
        int h = ctx.h;
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
        for (int i = 0; i < cartonFrameCount; i++) {
            float alpha = (float) ((i + 0.0) / cartonFrameCount);
            // log.info("当前透明度 : " + alpha);

            Graphics2D g2d = image.createGraphics();
            // g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)(1 - alpha)));
            g2d.clearRect(0, 0, w, h);
            g2d.drawImage(ctx.cur.image, 0, 0, w, h, null);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2d.drawImage(ctx.next.image, 0, 0, w, h, null);
            g2d.dispose();
            Images.writeJpeg(image, nextFile(ctx), 0.9f);
        }
    }
}
