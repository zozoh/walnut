package org.nutz.walnut.ext.kml.hdl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.nutz.lang.Strings;
import org.nutz.lang.Times;
import org.nutz.plugins.xmlbind.XmlBind;
import org.nutz.walnut.ext.kml.bean.KmlFile;
import org.nutz.walnut.ext.kml.bean.KmlFolder;
import org.nutz.walnut.ext.kml.bean.KmlPlacemark;
import org.nutz.walnut.ext.kml.bean.KmlPlacemarkLineString;
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

@JvmHdlParamArgs(value="cqn")
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
        String conv_from = hc.params.get("conv_from", "");
        String conv_to = hc.params.get("conv_to", "");
        Set<String> pointsKeyworks = new HashSet<>(Arrays.asList(hc.params.get("points", "标注点,航点").split(",")));
        Set<String> routeKeyworks = new HashSet<>(Arrays.asList(hc.params.get("route", "导航线,轨迹,航迹").split(",")));
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
                        if (!Strings.isBlank(placemark.name)) {
                            if (placemark.name.toString().startsWith("CP")) {
                                point.type = "cp";
                            }
                            else if (placemark.name.toString().startsWith("SP")) {
                                point.type = "sp";
                            }
                        }
                        else {
                            point.type = "sp";
                        }
                        if (Strings.isBlank(point.name))
                            point.name = placemark.name;
                        point.desc = placemark.description;
                        double[] tmp = WoozTools.parse(placemark.point.coordinates);
                        point.lng = tmp[0];
                        point.lat = tmp[1];
                        point.ele = tmp[2];
                        WoozTools.convert(point, conv_from, conv_to);
                        wooz.points.add(point);
                    }
                }
                else if (routeKeyworks.contains(folder.name)) {
                    wooz.route = new ArrayList<>();
                    List<KmlPlacemark> placemarks = folder.placemarks;
                    if (placemarks == null) {
                        if (folder.folders != null) {
                            placemarks = folder.folders.get(0).placemarks;
                        }
                    }
                    KmlPlacemark placemark = placemarks.get(0);
                    KmlPlacemarkLineString lineString = placemark.lineString;
                    if (lineString == null && placemark.MultiGeometry != null) {
                        lineString = placemark.MultiGeometry.LineString;
                    }
                    if (lineString != null) {
                        String[] tmp2 = lineString.coordinates.split(" ");
                        for (String coordinate : tmp2) {
                            if (Strings.isBlank(coordinate))
                                continue;
                            double[] tmp = WoozTools.parse(coordinate);
                            WoozRoute route = new WoozRoute();
                            route.lng = tmp[0];
                            route.lat = tmp[1];
                            route.ele = tmp[2];
                            WoozTools.convert(route, conv_from, conv_to);
                            wooz.route.add(route);
                        }
                    }
                    else if (placemark.track != null) {
                        int index = 0;
                        List<String> whens = placemark.track.whens;
                        for (String coord : placemark.track.coords) {
                            double[] tmp = WoozTools.parse(coord.replace(' ', ','));
                            WoozRoute route = new WoozRoute();
                            route.lng = tmp[0];
                            route.lat = tmp[1];
                            route.ele = tmp[2];
                            WoozTools.convert(route, conv_from, conv_to);
                            wooz.route.add(route);
                            try {
                                if (whens != null && index < whens.size()) {
                                    route.time = Times.parse("yyyy-MM-dd'T'HH:mm:ss'Z'", whens.get(index));
                                }
                            }
                            catch (Throwable e) {
                            }
                            index++;
                        }
                    }
                }
            }
            sys.out.writeJson(wooz, Cmds.gen_json_format(hc.params));
        }
    }

}
