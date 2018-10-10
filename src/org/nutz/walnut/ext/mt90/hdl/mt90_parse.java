package org.nutz.walnut.ext.mt90.hdl;

import java.io.BufferedReader;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.lang.Times;
import org.nutz.plugins.xmlbind.XmlBind;
import org.nutz.walnut.ext.gpx.bean.GpxFile;
import org.nutz.walnut.ext.gpx.bean.GpxTrk;
import org.nutz.walnut.ext.gpx.bean.GpxTrkpt;
import org.nutz.walnut.ext.gpx.bean.GpxTrkseg;
import org.nutz.walnut.ext.kml.bean.KmlDocument;
import org.nutz.walnut.ext.kml.bean.KmlFile;
import org.nutz.walnut.ext.kml.bean.KmlPlacemark;
import org.nutz.walnut.ext.kml.bean.KmlPlacemarkLineString;
import org.nutz.walnut.ext.kml.bean.KmlPlacemarkPoint;
import org.nutz.walnut.ext.kml.bean.KmlStyle;
import org.nutz.walnut.ext.kml.bean.KmlStyleLineStyle;
import org.nutz.walnut.ext.mt90.bean.Mt90Raw;
import org.nutz.walnut.ext.wooz.WoozRoute;
import org.nutz.walnut.ext.wooz.WoozTools;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wn;

@JvmHdlParamArgs(value="cqn", regex="^(kml|gpx|gpsFixed|simple|lineOnly)$")
public class mt90_parse implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        Reader r;
        if (sys.pipeId > 0) {
            r =  sys.in.getReader();
        }
        else {
            r = sys.io.getReader(sys.io.check(null, Wn.normalizeFullPath(hc.params.val_check(0), sys)), 0);
        }
        BufferedReader br = new BufferedReader(r);
        List<Mt90Raw> list = new ArrayList<>();
        boolean onlyGpsFixed = hc.params.is("gpsFixed");
        long begin = hc.params.has("begin") ? Times.ams(hc.params.get("begin")) - 8*3600*1000L : 0;
        long end = hc.params.has("end") ? Times.ams(hc.params.get("end")) - 8*3600*1000L : Long.MAX_VALUE;
        boolean simple = hc.params.is("simple");
        int speed = hc.params.getInt("speed", 300);
        String name = hc.params.get("name");
        boolean lineOnly = hc.params.is("lineOnly");
        while (true) {
            String line = br.readLine();
            if (line == null)
                break;
            if (Strings.isBlank(line))
                continue;
            Mt90Raw raw = Mt90Raw.mapping(line);
            // TODO 过滤特定时段
            if (raw != null) {
                // 是否GPS定位的结果
                if (onlyGpsFixed && !"A".equals(raw.gpsFixed)) {
                    continue;
                }
                // 超过设置的时间了吗?
                if (raw.timestamp < begin || raw.timestamp > end) {
                    continue;
                }
                //System.out.println("" + raw.timestamp + "," + begin);
                // 是否超过正常速度
                if (raw.speed > speed) {
                    continue;
                }
                // 简单模式,需要进行坐标转换
                if (simple) {
                    WoozRoute route = new WoozRoute();
                    route.lat = raw.lat;
                    route.lng = raw.lng;
                    WoozTools.convert(route, "wgs84", "gcj02");
                    raw.lat = route.lat;
                    raw.lng = route.lng;
                }
                list.add(raw);
            }
        }
        // 按gps时间排序
        Collections.sort(list);
        // 计算名称
        if (!simple && Strings.isBlank(name)) {
            if (list.size() > 0) {
                name = "MT90-" + new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.UK).format(new Date(list.get(0).timestamp));
            }
            else {
                name = "MT90";
            }
        }
        if (hc.params.is("gpx")) {
            GpxFile gpx = new GpxFile();
            gpx.trk = new GpxTrk();
            gpx.trk.name = name;
            gpx.trk.trkseg = new GpxTrkseg();
            gpx.trk.trkseg.trkpts = new ArrayList<>(list.size());
            for (Mt90Raw raw : list) {
                GpxTrkpt trkpt = new GpxTrkpt();
                trkpt.ele = raw.ele + "";
                trkpt.lat = raw.lat + "";
                trkpt.lon = raw.lng + "";
                // 2009-10-17T18:37:34Z
                trkpt.time = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.UK).format(new Date(raw.timestamp));
                gpx.trk.trkseg.trkpts.add(trkpt);
            }
            String str = XmlBind.toXml(gpx, "gpx");
            sys.out.print(str);
            return;
        }
        if (hc.params.is("kml")) {
            KmlFile kml = new KmlFile();
            kml.document = new KmlDocument();
            kml.document.name = name;
            kml.document.open = "1";
            // 添加默认样式
            kml.document.styles = new ArrayList<>();
            KmlStyle yellowLineGreenPoly = new KmlStyle();
            yellowLineGreenPoly.id = "yellowLineGreenPoly";
            yellowLineGreenPoly.lineStyle = new KmlStyleLineStyle();
            yellowLineGreenPoly.lineStyle.width = "4";
            yellowLineGreenPoly.lineStyle.color = "7f00ffff";
            kml.document.styles.add(yellowLineGreenPoly);
            // 添加轨迹线和轨迹点
            kml.document.placemarks = new ArrayList<>(list.size()+10);
            KmlPlacemark first = new KmlPlacemark();
            first.name = "Line";
            first.lineString = new KmlPlacemarkLineString();
            first.lineString.tessellate = "1";
            first.lineString.altitudeMode = "relativeToGround";
            first.styleUrl = "#yellowLineGreenPoly";
            StringBuilder coordinates = new StringBuilder();
            kml.document.placemarks.add(first);
            for (Mt90Raw raw : list) {
                KmlPlacemark placemark = new KmlPlacemark();
                placemark.point = new KmlPlacemarkPoint();
                placemark.name = String.format("%dkm/h %s", raw.speed, Times.sDT(new Date(raw.timestamp)));
                //<coordinates>116.287656,39.894523,0</coordinates>
                placemark.point.coordinates = String.format("%s,%s,%s", raw.lng, raw.lat, raw.ele);
                if (!lineOnly)
                    kml.document.placemarks.add(placemark);
                coordinates.append(placemark.point.coordinates).append("\r\n");
            }
            first.lineString.coordinates = coordinates.toString();
            String str = XmlBind.toXml(kml, "kml");
            sys.out.print(str);
            return;
        }
        if (simple) {
            JsonFormat jf = JsonFormat.full().setCompact(true).setActived("^(lat|lng|ele|timestamp)$");
            sys.out.writeJson(list, jf);
        }
        else {
            sys.out.writeJson(list, Cmds.gen_json_format(hc.params));
        }
        
    }
}
