package org.nutz.walnut.ext.timg.builder;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.nutz.img.Images;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.ext.timg.AbstractCartonBuilder;
import org.nutz.walnut.ext.timg.CartonCtx;

/**
 * 各种方向的移动,透明度变化
 *
 */
public class MoovCartonBuilder extends AbstractCartonBuilder {
    
    protected static final Log log = Logs.get();

    public MoovParam curParam;
    public MoovParam nextParam;
    public boolean drawCurFirst;

    public MoovCartonBuilder(MoovParam curParam, MoovParam nextParam, boolean drawCurFirst) {
        this.curParam = curParam;
        this.nextParam = nextParam;
        this.drawCurFirst = drawCurFirst;
    }

    @Override
    public void _invoke(CartonCtx ctx) {
        int cartonFrameCount = ctx.cur.cartonTime / (1000 / ctx.fps) + 1;
        log.infof("转场共%d帧", cartonFrameCount);
        int w = ctx.w;
        int h = ctx.h;
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
        cartonFrameCount -= 14;
        for (int i = 0; i < cartonFrameCount; i++) {

            // 首先,清图
            Graphics2D g2d = image.createGraphics();
            g2d.clearRect(0, 0, w, h);

            // 计算进度
            double process = (i + 0.0) / cartonFrameCount;
            
            // 先画第一张图?
            if (drawCurFirst) {
                drawImage(g2d, process, ctx.cur.image, curParam, w, h, false);
                drawImage(g2d, process, ctx.next.image, nextParam, w, h, true);
            }
            else {
                drawImage(g2d, process, ctx.next.image, nextParam, w, h, true);
                drawImage(g2d, process, ctx.cur.image, curParam, w, h, false);
            }
            
            g2d.dispose();
            Images.writeJpeg(image, nextFile(ctx), 0.9f);
        }
        exportNext(ctx, 14);
    }
    
    public void drawImage(Graphics2D g2d, double process, BufferedImage image, MoovParam param, int w, int h, boolean moovToZero) {
        // 透明度
        float alpha = (float) compute(process, param.alpha, 1);
        //System.out.println("" + process + "," + param.alpha + "," + alpha);
        // X/Y位置
        int x = 0;
        int y = 0;
        if (moovToZero) {
            x = (int) ( (1- process) * param.x * w);
            y = (int) ( (1- process) * param.y * h);
        }
        else {
            x = (int) ( (process) * param.x * w);
            y = (int) ( (process) * param.y * h);
        }
        int size_w = (int) compute(process, param.zoom, w);
        int size_h = (int) compute(process, param.zoom, h);
        
        //log.infof("透明度%s 位置(%d,%d) 大小(%d,%d)", alpha, x, y, size_w, size_h);
        
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2d.drawImage(image, x, y, size_w, size_h, null);
    }
    
    public static double compute(double process, int type, double base) {
        switch (type) {
        case 0:
            return base;
        case 1:
            return process * base;
        case -1:
        default:
            return (1 - process) * base;
        }
    }
    
    public static class MoovParam {
        public int x; // 0居中, -1 左侧, 1 右侧
        public int y; // 0居中, -1 下方, 1 上方
        public int alpha; // 透明度方向, 0 不变, -1,变淡, 1变浓
        public int rotation; // 旋转方向, 0 不变, -1,顺时针, 1逆时针
        public int zoom; // 放大缩小, 0 不变, -1,放大, 1缩小
        
        public MoovParam(int x, int y, int alpha, int rotation, int zoom) {
            this.x =x;
            this.y = y;
            this.alpha = alpha;
            this.rotation = rotation;
            this.zoom = zoom;
        }
        public MoovParam() {
        }
    }
    
    public static class Builder {
        public MoovParam curParam = new MoovParam();
        public MoovParam nextParam = new MoovParam();
        public boolean drawCurFirst;
        public static Builder create(boolean drawCurFirst) {
            Builder builder = new Builder();
            builder.drawCurFirst = drawCurFirst;
            return builder;
        }
        public MoovCartonBuilder build() {
            return new MoovCartonBuilder(curParam, nextParam, drawCurFirst);
        }
        // 位置变化
        public Builder moov(int curX, int curY, int nextX, int nextY) {
            curParam.x = curX;
            curParam.y = curY;
            nextParam.x = nextX;
            nextParam.y = nextY;
            return this;
        }
        // 透明度变化
        public Builder alpha(int cur, int next) {
            curParam.alpha = cur;
            nextParam.alpha = next;
            return this;
        }
        // TODO 缩放
        public Builder zoom(int cur, int next) {
            curParam.zoom = cur;
            nextParam.zoom = next;
            return this;
        }
    }
}
