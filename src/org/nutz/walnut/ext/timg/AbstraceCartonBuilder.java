package org.nutz.walnut.ext.timg;

import java.io.ByteArrayOutputStream;
import java.io.File;

import org.nutz.img.Images;
import org.nutz.lang.Files;

public abstract class AbstraceCartonBuilder implements CartonBuilder {

    public void add(CartonCtx ctx, byte[] buf) {
        File f = Files.createFileIfNoExists(new File(String.format("%s/images/T%06d.jpg", ctx.tmpDir, ctx.lastFrameIndex)));
        Files.write(f, buf);
        ctx.lastFrameIndex ++;
    }
    
    public File nextFile(CartonCtx ctx) {
        File f = Files.createFileIfNoExists(new File(String.format("%s/images/T%06d.jpg", ctx.tmpDir, ctx.lastFrameIndex)));
        ctx.lastFrameIndex ++;
        return f;
    }
    
    public void exportOrigin(CartonCtx ctx, int count) {
        //输出playTime,原图
        if (count > 0) {
            ByteArrayOutputStream out = new ByteArrayOutputStream(1024*1024);
            Images.writeJpeg(ctx.cur.image, out, 0.9f);
            byte[] buf = out.toByteArray();
            for (int i = 0; i < count; i++) {
                add(ctx, buf);
            }
        }
    }
    
    public void prepareImages(CartonCtx ctx) {
        if (ctx.cur.image == null)
            ctx.cur.image = ctx.io.readImage(ctx.cur.wobj);
        if (ctx.next.image == null)
            ctx.next.image = ctx.io.readImage(ctx.next.wobj);
        // 首先
        if (ctx.w < 1)
            ctx.w = ctx.cur.image.getWidth();
        if (ctx.h < 1)
            ctx.h = ctx.cur.image.getHeight();
    }
}
