package org.nutz.walnut.ext.geo.gpx.hdl;

import java.util.ArrayList;

import org.nutz.plugins.xmlbind.XmlBind;
import org.nutz.walnut.ext.geo.gpx.bean.GpxFile;
import org.nutz.walnut.ext.geo.gpx.bean.GpxTrkpt;
import org.nutz.walnut.ext.geo.kml.bean.KmlDocument;
import org.nutz.walnut.ext.geo.kml.bean.KmlFile;
import org.nutz.walnut.ext.geo.kml.bean.KmlPlacemark;
import org.nutz.walnut.ext.geo.kml.bean.KmlPlacemarkLineString;
import org.nutz.walnut.ext.geo.kml.bean.KmlPlacemarkPoint;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public class gpx_tokml implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        String text = null;
        if (sys.pipeId > 0) {
            text = sys.in.readAll();
        }
        else {
            text = sys.io.readText(sys.io.check(null, Wn.normalizeFullPath(hc.params.val_check(0), sys)));
        }
        GpxFile gpx = XmlBind.fromXml(GpxFile.class, text);
        KmlFile kml = new KmlFile();
        kml.document = new KmlDocument();
        kml.document.name = "MT90";
        kml.document.open = "1";
        kml.document.placemarks = new ArrayList<>();
        
        KmlPlacemark first = new KmlPlacemark();
        first.name = "Line";
        first.lineString = new KmlPlacemarkLineString();
        StringBuilder coordinates = new StringBuilder();
        kml.document.placemarks.add(first);
        if (gpx.trk != null && gpx.trk.trkseg != null && gpx.trk.trkseg.trkpts != null && gpx.trk.trkseg.trkpts.size() > 0) {
            for (GpxTrkpt trkpt : gpx.trk.trkseg.trkpts) {
                KmlPlacemark placemark = new KmlPlacemark();
                placemark.name = "Point";
                placemark.point = new KmlPlacemarkPoint();
                placemark.point.coordinates = String.format("%s,%s,%s", trkpt.lon, trkpt.lat, trkpt.ele);
                kml.document.placemarks.add(placemark);
                coordinates.append(placemark.point.coordinates).append("\r\n");
            }
            sys.out.print(XmlBind.toXml(kml));
        }
    }

}
