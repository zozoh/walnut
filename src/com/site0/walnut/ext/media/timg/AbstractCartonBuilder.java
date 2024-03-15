package com.site0.walnut.ext.media.timg;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.nutz.img.Images;
import org.nutz.lang.Files;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.segment.Segment;
import org.nutz.lang.segment.Segments;
import org.nutz.log.Log;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.api.io.WnObj;

public abstract class AbstractCartonBuilder implements CartonBuilder {
    
    protected static final Log log = Wlog.getCMD();

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
            ctx.cur.image = readImage(ctx, ctx.cur.wobj, true);
        if (ctx.next.image == null)
            ctx.next.image = readImage(ctx, ctx.next.wobj, false);
        
        if (ctx.cur.image == null)
            throw new RuntimeException("当前播放对象的图像无法获取");
        if (ctx.next.image == null)
            throw new RuntimeException("下个播放对象的图像无法获取");
        // 首先
        if (ctx.w < 1)
            ctx.w = ctx.cur.image.getWidth();
        if (ctx.h < 1)
            ctx.h = ctx.cur.image.getHeight();
    }
    
    public BufferedImage readImage(CartonCtx ctx, WnObj wobj, boolean cur) {
        log.info("read from carton :" + wobj.path());
        if (wobj.mime().startsWith("image/"))
            return ctx.io.readImage(wobj);
        if (wobj.mime().startsWith("video/")) {
            Segment seg = Segments.create("ffmpeg -y -v quiet -ss ${frame} -i ${source} -f image2 -vframes 1 ${thumbPath}");
            Files.write(new File(ctx.tmpDir + "/tmp.mp4"), ctx.io.getInputStream(wobj, 0));
            seg.add("source", ctx.tmpDir + "/tmp.mp4");
            seg.add("thumbPath", ctx.tmpDir + "/video_frame.png");
            if (cur) {
                seg.add("frame", toFFmpegTime(ctx.videoFrame, 24));
            }
            else {
                seg.add("frame", "00:00:00.00");
            }
            log.info("CMD: " + seg.render());
            String[] cmd = seg.render().toString().split(" ");
            try {
                Wlang.execOutput(cmd);
            }
            catch (IOException e) {
            }
            File t = new File(ctx.tmpDir + "/video_frame.png");
            log.info("PNG: " + t.getAbsolutePath() + " " + t.exists());
            return Images.read(t);
        }
        log.info("我靠,返回null!!");
        return null;
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
    
    public static String toFFmpegTime(int frameCount, int fps) {
        if (frameCount < 1)
            return "00:00:00.00";
        if (fps < 1)
            fps = 24;
        int min = frameCount / 24 /24;
        int sec = (frameCount - min * 24) / 24;
        int ms = frameCount % 24 * 4;
        String t = String.format("00:%02d:%02d.%02d", min, sec, ms);
        return t;
    }
}
