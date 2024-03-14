package com.site0.walnut.ext.geo.gpx.hdl;

import java.util.ArrayList;
import java.util.Date;

import org.nutz.lang.Strings;
import org.nutz.lang.Times;
import org.nutz.lang.util.Regex;
import org.nutz.plugins.xmlbind.XmlBind;
import com.site0.walnut.ext.geo.gpx.bean.GpxFile;
import com.site0.walnut.ext.geo.gpx.bean.GpxTrk;
import com.site0.walnut.ext.geo.gpx.bean.GpxTrkpt;
import com.site0.walnut.ext.geo.gpx.bean.GpxTrkseg;
import com.site0.walnut.ext.geo.kml.bean.KmlFile;
import com.site0.walnut.ext.geo.kml.bean.KmlPlacemark;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;

public class gpx_fromkml implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        String text = null;
        if (sys.pipeId > 0) {
            text = sys.in.readAll();
        }
        else {
            text = sys.io.readText(sys.io.check(null, Wn.normalizeFullPath(hc.params.val_check(0), sys)));
        }
        KmlFile kml = XmlBind.fromXml(KmlFile.class, text);

        GpxFile gpx = new GpxFile();
        gpx.trk = new GpxTrk();
        gpx.trk.trkseg = new GpxTrkseg();
        gpx.trk.trkseg.trkpts = new ArrayList<>();
        for (KmlPlacemark placemark : kml.document.placemarks) {
            if (placemark.point == null)
                continue;
            GpxTrkpt trkpt = new GpxTrkpt();
            String[] tmp = placemark.point.coordinates.split(",");
            trkpt.lat = tmp[0];
            trkpt.lon = tmp[1];
            trkpt.ele = tmp[2];
            gpx.trk.trkseg.trkpts.add(trkpt);
            if (placemark.name != null) {
                if (Regex.match("^[0-9]+km/h", placemark.name)) {
                    String[] tmp2 = Strings.splitIgnoreBlank(placemark.name, " ");
                    try {
                        int speed = Integer.parseInt(tmp2[0].substring(0, tmp2[0].indexOf("km"))) * 1000;
                        trkpt.speed = ""+speed;
                        // <name>0km/h  2018-08-12 07:33:27</name>
                        // <time>2002-02-10T21:01:29.250Z</time>
                        Date date = Times.parse("yyyy-MM-dd HH:mm:ss", tmp2[1] + " " + tmp2[2]);
                        trkpt.time = Times.format("yyyy-MM-dd'T'HH:mm:ss'Z'", date);
                    }
                    catch (Throwable e) {
                    }
                }
            }
        }
        sys.out.print(XmlBind.toXml(gpx));
        return;
    }

}
