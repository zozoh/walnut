package org.nutz.walnut.ext.timg;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.nutz.img.Images;
import org.nutz.lang.Files;

public abstract class AbstractCartonBuilder implements CartonBuilder {

    public void add(CartonCtx ctx, byte[] buf) {
        File f = Files.createFileIfNoExists(new File(String.format("%s/images/T%06d.png", ctx.tmpDir, ctx.lastFrameIndex)));
        Files.write(f, buf);
        ctx.lastFrameIndex ++;
    }
    
    public File nextFile(CartonCtx ctx) {
        File f = Files.createFileIfNoExists(new File(String.format("%s/images/T%06d.png", ctx.tmpDir, ctx.lastFrameIndex)));
        ctx.lastFrameIndex ++;
        return f;
    }
    
    public void exportOrigin(CartonCtx ctx, int count) {
        //输出playTime,原图
        if (count > 0) {
            ByteArrayOutputStream out = new ByteArrayOutputStream(1024*1024);
            Images.write(ctx.cur.image, "png", out);
            byte[] buf = out.toByteArray();
            for (int i = 0; i < count; i++) {
                add(ctx, buf);
            }
        }
    }
    
    public void exportNext(CartonCtx ctx, int count, int drawNumberFrom, boolean drawNumber) {
        //输出playTime,原图
        if (count > 0) {
            if (drawNumber) {
                for (int i = 0; i < count; i++) {
                    BufferedImage image = new BufferedImage(ctx.next.image.getWidth(), ctx.next.image.getHeight(), ctx.next.image.getType());
                    Graphics2D g2d = image.createGraphics();
                    g2d.drawImage(ctx.next.image, 0, 0, image.getWidth(), image.getHeight(), null);
                    g2d.setColor(Color.BLACK);
                    g2d.setFont(chineseFont);
                    g2d.drawString("O" + (i + drawNumberFrom), image.getWidth()/2, image.getHeight()/2);
                    g2d.dispose();
                    ByteArrayOutputStream out = new ByteArrayOutputStream(1024*1024);
                    Images.write(image, "png", out);
                    byte[] buf = out.toByteArray();
                    add(ctx, buf);
                }
            }
            else {
                ByteArrayOutputStream out = new ByteArrayOutputStream(1024*1024);
                Images.write(ctx.next.image, "png", out);
                byte[] buf = out.toByteArray();
                for (int i = 0; i < count; i++) {
                    add(ctx, buf);
                }
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
    
    @Override
    public final void invoke(CartonCtx ctx) {
        prepareImages(ctx);
        exportOrigin(ctx, ctx.cur.playTime / (1000 / ctx.fps));
        // 输出转场的部分
        if (ctx.cur.cartonTime < 1) {
            return;
        }
        _invoke(ctx);
    }
    
    public abstract void _invoke(CartonCtx ctx);
    

    
    public static Font chineseFont;
    static {
        List<String> fonts = new ArrayList<String>();
        fonts.add("/system/fonts/DroidSansFallback.ttf");
        fonts.add("/usr/share/fonts/truetype/wqy-zenhei.ttc");
        fonts.add("/usr/share/fonts/truetype/wqy/wqy-zenhei.ttc");
        fonts.add("/usr/share/fonts/truetype/DroidSansFallbackFull.ttf");
        for (String path : fonts) {
            try {
                File f = new File(path);
                if (f.exists()) {
                    chineseFont = Font.createFont(0, f).deriveFont(Font.BOLD, 256);
                    break;
                }
            }
            catch (Throwable e) {}
        }
        if (chineseFont == null)
            chineseFont = new Font("宋体",Font.BOLD, 256);
    }
}
