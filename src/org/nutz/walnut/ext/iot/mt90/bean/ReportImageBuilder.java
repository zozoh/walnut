package org.nutz.walnut.ext.iot.mt90.bean;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class ReportImageBuilder implements AutoCloseable {
    public int w;
    public int h;
    public BufferedImage image;
    public Graphics2D g2d;
    public int[] zeroPoint;
    public int dataIndex = 0;
    public int points;
    public int accuracy;
    public int[] pointsX;

    public ReportImageBuilder(int points, int accuracy, Color bgColor) {
        w = points + 200;
        h = accuracy + 100;
        image = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
        g2d = image.createGraphics();
        g2d.setColor(bgColor);
        g2d.fillRect(0, 0, w, h);
        zeroPoint = new int[]{150, h - 50};
        this.points = points;
        this.accuracy = accuracy;
        this.pointsX = new int[points];
        for (int i = 0; i < pointsX.length; i++) {
            pointsX[i] = 150 + i;
        }
    }

    /**
     * 画坐标系
     */
    public void drawCoordinate(Color color) {
        g2d.setColor(color);
        g2d.drawLine(zeroPoint[0], zeroPoint[1], zeroPoint[0], 50);
        g2d.drawLine(zeroPoint[0], zeroPoint[1], w - 50, zeroPoint[1]);
    }

    public void drawData(String name, Color color, double[] datas, int max, int min) {
        g2d.setColor(color);
        int _label_y = dataIndex * 15 + 30;
        g2d.drawString(name, 10, _label_y);
        g2d.drawLine(2, _label_y, 8, _label_y);
        int[] pointsY = new int[datas.length];
        double t = accuracy / (max - min + 0.0);
        for (int i = 0; i < pointsY.length; i++) {
            double tmp = datas[i];
            if (tmp > max)
                tmp = max;
            else if (tmp < min)
                tmp = min;
            tmp -= min;

            pointsY[i] = h - 50 - (int) (t * tmp);
        }
        g2d.drawPolyline(pointsX, pointsY, datas.length);
        dataIndex ++;
    }

    public BufferedImage getImage() {
        return image;
    }

    public void close() {
        g2d.dispose();
    }

    public ReportImageBuilder begin() {
        g2d = image.createGraphics();
        return this;
    }
}