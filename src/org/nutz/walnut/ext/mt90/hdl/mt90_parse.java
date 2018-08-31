package org.nutz.walnut.ext.mt90.hdl;

import java.io.BufferedReader;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.nutz.lang.Strings;
import org.nutz.lang.Times;
import org.nutz.plugins.xmlbind.entity.XmlEntityAnnotationMaker;
import org.nutz.walnut.ext.gpx.bean.GpxFile;
import org.nutz.walnut.ext.gpx.bean.GpxTrk;
import org.nutz.walnut.ext.gpx.bean.GpxTrkpt;
import org.nutz.walnut.ext.gpx.bean.GpxTrkseg;
import org.nutz.walnut.ext.kml.bean.KmlDocument;
import org.nutz.walnut.ext.kml.bean.KmlFile;
import org.nutz.walnut.ext.kml.bean.KmlPlacemark;
import org.nutz.walnut.ext.kml.bean.KmlPlacemarkLineString;
import org.nutz.walnut.ext.kml.bean.KmlPlacemarkPoint;
import org.nutz.walnut.ext.mt90.bean.Mt90Raw;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;

@JvmHdlParamArgs(value="cqn", regex="^(kml|gpx|gpsOk)$")
public class mt90_parse implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        String text = sys.in.readAll();
        BufferedReader br = new BufferedReader(new StringReader(text));
        List<Mt90Raw> list = new ArrayList<>();
        while (br.ready()) {
            String line = br.readLine();
            if (line == null)
                break;
            if (Strings.isBlank(line))
                continue;
            Mt90Raw raw = Mt90Raw.mapping(line);
            // TODO 过滤特定时段
            if (raw != null) {
                if (hc.params.is("gpsOk") && !"A".equals(raw.gpsLoc)) {
                    continue;
                }
                list.add(raw);
            }
        }
        // 按gps时间排序
        Collections.sort(list);
        if (hc.params.is("gpx")) {
            GpxFile gpx = new GpxFile();
            gpx.trk = new GpxTrk();
            gpx.trk.name = "MT90";
            gpx.trk.trkseg = new GpxTrkseg();
            gpx.trk.trkseg.trkpts = new ArrayList<>(list.size());
            for (Mt90Raw raw : list) {
                GpxTrkpt trkpt = new GpxTrkpt();
                trkpt.ele = raw.ele + "";
                trkpt.lat = raw.lat;
                trkpt.lon = raw.lng;
                // 2009-10-17T18:37:34Z
                trkpt.time = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(raw.gpsDate);
                gpx.trk.trkseg.trkpts.add(trkpt);
            }
            String str = new XmlEntityAnnotationMaker().makeEntity(null, GpxFile.class).write(gpx, "gpx");
            sys.out.print(str);
            return;
        }
        if (hc.params.is("kml")) {
            KmlFile kml = new KmlFile();
            kml.document = new KmlDocument();
            kml.document.name = "MT90";
            kml.document.open = "1";
            kml.document.placemarks = new ArrayList<>(list.size()+10);
            KmlPlacemark first = new KmlPlacemark();
            first.name = "Line";
            first.lineString = new KmlPlacemarkLineString();
            StringBuilder coordinates = new StringBuilder();
            kml.document.placemarks.add(first);
            for (Mt90Raw raw : list) {
                KmlPlacemark placemark = new KmlPlacemark();
                placemark.point = new KmlPlacemarkPoint();
                placemark.name = String.format("%dkm/h %s", raw.speed, Times.sDT(raw.gpsDate));
                //coordinates>116.287656,39.894523,0</coordinates>
                placemark.point.coordinates = String.format("%s,%s,%s", raw.lng, raw.lat, raw.ele);
                kml.document.placemarks.add(placemark);
                coordinates.append(placemark.point.coordinates).append("        \r\n");
            }
            first.lineString.coordinates = coordinates.toString();
            String str = new XmlEntityAnnotationMaker().makeEntity(null, KmlFile.class).write(kml, "kml");
            sys.out.print(str);
            return;
        }
        
        sys.out.writeJson(list, Cmds.gen_json_format(hc.params));
    }
}
