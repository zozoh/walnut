package com.site0.walnut.ext.geo.kml.hdl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.nutz.lang.Strings;
import org.nutz.plugins.xmlbind.XmlBind;
import com.site0.walnut.ext.biz.wooz.WoozMap;
import com.site0.walnut.ext.biz.wooz.WoozPoint;
import com.site0.walnut.ext.biz.wooz.WoozRoute;
import com.site0.walnut.ext.biz.wooz.WoozTools;
import com.site0.walnut.ext.geo.kml.bean.KmlFile;
import com.site0.walnut.ext.geo.kml.bean.KmlFolder;
import com.site0.walnut.ext.geo.kml.bean.KmlPlacemark;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Wn;

@JvmHdlParamArgs(value="cqn")
public class kml_fromwooz implements JvmHdl {

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
        Set<String> pointsKeyworks = new HashSet<>(Arrays.asList(hc.params.get("points", "标注点").split(",")));
        Set<String> routeKeyworks = new HashSet<>(Arrays.asList(hc.params.get("route", "导航线,轨迹").split(",")));
        WoozMap wooz = new WoozMap();
        if (kml.document.folders != null) {
            for (KmlFolder folder : kml.document.folders) {
                if (pointsKeyworks.contains(folder.name)) {
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
                        double[] tmp = WoozTools.parse(placemark.point.coordinates);
                        point.lng = tmp[0];
                        point.lat = tmp[1];
                        point.ele = tmp[2];
                        wooz.points.add(point);
                    }
                }
                else if (routeKeyworks.contains(folder.name)) {
                    wooz.route = new ArrayList<>();
                    String[] tmp2 = folder.placemarks.get(0).lineString.coordinates.split(" ");
                    for (String coordinate : tmp2) {
                        if (Strings.isBlank(coordinate))
                            continue;
                        double[] tmp = WoozTools.parse(coordinate);
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
