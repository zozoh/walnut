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
import org.nutz.lang.util.NutMap;
import org.nutz.log.Logs;
import org.nutz.plugins.xmlbind.XmlBind;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.gpx.bean.GpxFile;
import org.nutz.walnut.ext.gpx.bean.GpxTrk;
import org.nutz.walnut.ext.gpx.bean.GpxTrkpt;
import org.nutz.walnut.ext.gpx.bean.GpxTrkseg;
import org.nutz.walnut.ext.kml.bean.KmlDocument;
import org.nutz.walnut.ext.kml.bean.KmlFile;
import org.nutz.walnut.ext.kml.bean.KmlFolder;
import org.nutz.walnut.ext.kml.bean.KmlGxTrack;
import org.nutz.walnut.ext.kml.bean.KmlPlacemark;
import org.nutz.walnut.ext.kml.bean.KmlStyle;
import org.nutz.walnut.ext.kml.bean.KmlStyleLineStyle;
import org.nutz.walnut.ext.mt90.Mt90Map;
import org.nutz.walnut.ext.mt90.bean.Mt90Raw;
import org.nutz.walnut.ext.wooz.WoozMap;
import org.nutz.walnut.ext.wooz.WoozPoint;
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
        WnObj trk_data = null;
        Reader r;
        if (sys.pipeId > 0) {
            r =  sys.in.getReader();
        }
        else {
            String path = Wn.normalizeFullPath(hc.params.val_check(0), sys);
            trk_data = sys.io.check(null, path);
            r = sys.io.getReader(trk_data, 0);
        }
        BufferedReader br = new BufferedReader(r);
        List<Mt90Raw> list = new ArrayList<>();
        boolean onlyGpsFixed = hc.params.is("gpsFixed");
        long begin = hc.params.has("begin") ? Times.ams(hc.params.get("begin")) - 8*3600*1000L : -1;
        long end = hc.params.has("end") ? Times.ams(hc.params.get("end")) - 8*3600*1000L : Long.MAX_VALUE;
        boolean simple = hc.params.is("simple");
        int speed = hc.params.getInt("speed", 300);
        String name = hc.params.get("name");
        String _map = hc.params.get("map");
        
        // 如果没有指定开始和结束使用,但指定了map, 获取赛事
        if (begin < 1 && !Strings.isBlank(_map)) {
            WoozMap map = Mt90Map.get(sys.io, Wn.normalizeFullPath(hc.params.get("map"), sys));
            if (map != null && map.points != null && map.points.size() > 1) {
                for (WoozPoint point : map.points) {
                    if ("start".equals(point.type)) {
                        if (!Strings.isBlank(point.closeAt)) {
                            // 2018-10-27T14:50:00
                            begin = Times.ams(point.closeAt) - 8*3600*1000L;
                        }
                    }
                    else if ("end".equals(point.type)) {
                        if (!Strings.isBlank(point.closeAt)) {
                            end = Times.ams(point.closeAt) - 8*3600*1000L;
                        }
                    }
                }
            }
        }
        //boolean lineOnly = hc.params.is("lineOnly");
        int goUp = 0;
        int goDown = 0;
        int goDistance = 0;
        Mt90Raw prev = null;
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
                    Logs.get().info("过滤一条记录" + line);
                    continue;
                }
                if (begin == -1) {
                    // 选择第一条记录的时间作为起点
                    begin = raw.timestamp;
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
                    if (prev != null) {
                        if (raw.ele  > prev.ele) {
                            goUp += raw.ele - prev.ele;
                        }
                        else {
                            goDown += prev.ele - raw.ele;
                        }
                        // TODO 过滤明显不合法的距离
                        double distance = WoozTools.getDistance(raw.lat, raw.lng, prev.lat, prev.lng);
                        if (distance > 10) {
                            goDistance += distance;
                        }
                    }
                    prev = raw;
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
            kml.document.folders = new ArrayList<>();
            // 添加轨迹
            KmlFolder TbuluTrackFolder = new KmlFolder();
            TbuluTrackFolder.id = "TbuluTrackFolder";
            TbuluTrackFolder.name = "轨迹";
            TbuluTrackFolder.placemarks = new ArrayList<>();
            KmlPlacemark placemark = new KmlPlacemark();
            placemark.name = "";
            placemark.track = new KmlGxTrack();
            placemark.track.coords = new ArrayList<>(list.size());
            placemark.track.whens = new ArrayList<>(list.size());
            for (Mt90Raw raw : list) {
                String coord = String.format("%s %s %s", raw.lng, raw.lat, raw.ele);
                placemark.track.coords.add(coord);
                placemark.track.whens.add(Times.format("yyyy-MM-dd'T'HH:mm:ss'Z'", new Date(raw.timestamp)));
            }
            TbuluTrackFolder.placemarks.add(placemark);
            kml.document.folders.add(TbuluTrackFolder);
            String str = XmlBind.toXml(kml, "kml");
            sys.out.print(str);
            return;
        }

        if (simple) {
            JsonFormat jf = JsonFormat.full().setCompact(true).setActived("^(lat|lng|ele|timestamp)$");
            sys.out.writeJson(list, jf);
            
            if (trk_data == null) {
                NutMap metas = new NutMap();
                metas.put("u_trk_go_up", goUp);
                metas.put("u_trk_go_down", goDown);
                metas.put("u_trk_go_distance", goDistance);
                sys.io.appendMeta(trk_data, metas);
            }
        }
        else {
            sys.out.writeJson(list, Cmds.gen_json_format(hc.params));
        }
    }
}
