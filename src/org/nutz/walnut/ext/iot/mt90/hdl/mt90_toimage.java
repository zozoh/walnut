package org.nutz.walnut.ext.iot.mt90.hdl;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.biz.wooz.WoozMap;
import org.nutz.walnut.ext.biz.wooz.WoozPoint;
import org.nutz.walnut.ext.biz.wooz.WoozRoute;
import org.nutz.walnut.ext.iot.mt90.Mt90Map;
import org.nutz.walnut.ext.iot.mt90.bean.Mt90Raw;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

@JvmHdlParamArgs(value="cqn", regex="^(kml|gpx|gpsFixed|simple|lineOnly|debug|tomap|all)$")
public class mt90_toimage extends mt90_parse {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        hc.params.setv("simple", true);
        WoozMap map = null;
        if (hc.params.has("map")) {
            map = Mt90Map.get(sys.io, Wn.normalizeFullPath(hc.params.check("map"), sys));
        }
        List<Mt90Raw> list = parse(sys, hc);
        
        double max_lat = Double.MIN_VALUE;
        double max_lng = Double.MIN_VALUE;
        double min_lat = Double.MAX_VALUE;
        double min_lng = Double.MAX_VALUE;
        for (Mt90Raw raw : list) {
            max_lat = Math.max(max_lat, raw.lat);
            max_lng = Math.max(max_lng, raw.lng);
            min_lat = Math.min(min_lat, raw.lat);
            min_lng = Math.min(min_lng, raw.lng);
        }
        if (map != null) {
            for (WoozRoute route : map.route) {
                max_lat = Math.max(max_lat, route.lat);
                max_lng = Math.max(max_lng, route.lng);
                min_lat = Math.min(min_lat, route.lat);
                min_lng = Math.min(min_lng, route.lng);
            }
        }

        double diff_lat = max_lat - min_lat; // 纬度, y
        double diff_lng = max_lng - min_lng; // 经度, x
        
        // 设定图片宽1920
        int image_ext = 100;
        int image_w = 5000;
        int image_h = (int) (image_w * (diff_lng / diff_lat)) / 2 * 2;
        BufferedImage image = new BufferedImage(image_w + image_ext, image_h + image_ext, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g2d = image.createGraphics();
        g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
        
        // 画选手
        g2d.setColor(Color.BLUE);
        int[] xPoints = new int[list.size()];
        int[] yPoints = new int[list.size()];
        for (int i = 0; i < xPoints.length; i++) {
            Mt90Raw raw = list.get(i);
            int x = (int) ((raw.lng - min_lng) * (image_w / diff_lng));
            int y = (int) ((raw.lat - min_lat) * (image_h / diff_lat));
            xPoints[i] = x + image_ext/2;
            yPoints[i] = image_h - y + image_ext/2;
            g2d.fillArc(xPoints[i], yPoints[i], 4, 4, 0, 360);
            g2d.drawString(""+i, xPoints[i], yPoints[i]);
        }
        g2d.drawPolygon(xPoints, yPoints, list.size());
        
        // 画原轨迹
        if (map != null) {
            g2d.setColor(Color.GREEN);
            xPoints = new int[map.route.size()];
            yPoints = new int[map.route.size()];
            for (int i = 0; i < xPoints.length; i++) {
                WoozRoute raw = map.route.get(i);
                int x = (int) ((raw.lng - min_lng) * (image_w / diff_lng));
                int y = (int) ((raw.lat - min_lat) * (image_h / diff_lat));
                xPoints[i] = x + image_ext/2;
                yPoints[i] = image_h - y + image_ext/2;
                g2d.drawString("M-" + i, xPoints[i] + 2, yPoints[i]+2);
            }
            g2d.setColor(Color.BLACK);
            g2d.drawPolygon(xPoints, yPoints, map.route.size());
            if (map.points != null) {
                g2d.setColor(Color.RED);
                for (WoozPoint point : map.points) {
                    int x = (int) ((point.lng - min_lng) * (image_w / diff_lng));
                    int y = (int) ((point.lat - min_lat) * (image_h / diff_lat));
                    g2d.drawString(point.name, x + image_ext/2, image_h - y + image_ext/2);
                }
            }
        }
        
        sys.io.writeImage(sys.io.createIfNoExists(null, Wn.normalizeFullPath(hc.params.check("image"), sys), WnRace.FILE), image);
    }
}
