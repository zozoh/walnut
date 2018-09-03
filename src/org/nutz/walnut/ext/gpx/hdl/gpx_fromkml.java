package org.nutz.walnut.ext.gpx.hdl;

import java.util.ArrayList;

import org.nutz.plugins.xmlbind.XmlBind;
import org.nutz.walnut.ext.gpx.bean.GpxFile;
import org.nutz.walnut.ext.gpx.bean.GpxTrk;
import org.nutz.walnut.ext.gpx.bean.GpxTrkpt;
import org.nutz.walnut.ext.gpx.bean.GpxTrkseg;
import org.nutz.walnut.ext.kml.bean.KmlFile;
import org.nutz.walnut.ext.kml.bean.KmlPlacemark;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

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
            sys.out.print(XmlBind.toXml(gpx));
            return;
        }
    }

}
