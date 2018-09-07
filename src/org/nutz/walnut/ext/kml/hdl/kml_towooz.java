package org.nutz.walnut.ext.kml.hdl;

import java.util.ArrayList;

import org.nutz.lang.Strings;
import org.nutz.plugins.xmlbind.XmlBind;
import org.nutz.walnut.ext.kml.bean.KmlFile;
import org.nutz.walnut.ext.kml.bean.KmlFolder;
import org.nutz.walnut.ext.kml.bean.KmlPlacemark;
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

@JvmHdlParamArgs(value="cqn", regex=("^(wgs84togcj02|wgs84tobd09)$"))
public class kml_towooz implements JvmHdl {

    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        String text = null;
        if (sys.pipeId > 0) {
            text = sys.in.readAll();
        }
        else {
            text = sys.io.readText(sys.io.check(null, Wn.normalizeFullPath(hc.params.val_check(0), sys)));
        }
        KmlFile kml = XmlBind.fromXml(KmlFile.class, text);
        if (kml.document == null)
            return;
        boolean _wgs84togcj02 = hc.params.is("wgs84togcj02");
        boolean _wgs84tobd09 = hc.params.is("wgs84tobd09");
        WoozMap wooz = new WoozMap();
        if (kml.document.folders != null) {
            for (KmlFolder folder : kml.document.folders) {
                if ("标注点".equals(folder.name)) {
                    wooz.points = new ArrayList<>();
                    for (KmlPlacemark placemark : folder.placemarks) {
                        WoozPoint point = new WoozPoint();
                        if ("startPoint".equals(placemark.id))
                            point.type = "start";
                        else if ("endPoint".equals(placemark.id))
                            point.type = "end";
                        else
                            point.type = "csp";
                        point.name = placemark.name;
                        point.desc = placemark.description;
                        double[] tmp = WoozTools.parse(placemark.point.coordinates, _wgs84togcj02, _wgs84tobd09);
                        point.lng = tmp[0];
                        point.lat = tmp[1];
                        point.ele = tmp[2];
                        wooz.points.add(point);
                    }
                }
                else if ("导航线".equals(folder.name)) {
                    wooz.route = new ArrayList<>();
                    String[] tmp2 = folder.placemarks.get(0).lineString.coordinates.split(" ");
                    for (String coordinate : tmp2) {
                        if (Strings.isBlank(coordinate))
                            continue;
                        double[] tmp = WoozTools.parse(coordinate, _wgs84togcj02, _wgs84tobd09);
                        WoozRoute route = new WoozRoute();
                        route.lng = tmp[0];
                        route.lat = tmp[1];
                        route.ele = tmp[2];
                        wooz.route.add(route);
                    }
                }
            }
            sys.out.writeJson(wooz, Cmds.gen_json_format(hc.params));
        }
    }

}
