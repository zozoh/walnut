package org.nutz.walnut.ext.gpx.hdl;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

import org.nutz.lang.Strings;
import org.nutz.lang.Times;
import org.nutz.lang.Xmls;
import org.nutz.plugins.xmlbind.entity.XmlEntity;
import org.nutz.plugins.xmlbind.entity.XmlEntityAnnotationMaker;
import org.nutz.walnut.ext.gpx.bean.GpxFile;
import org.nutz.walnut.ext.gpx.bean.GpxTrkpt;
import org.nutz.walnut.ext.gpx.bean.GpxWpt;
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
import org.w3c.dom.Element;

@JvmHdlParamArgs(value="cqn", regex="^(cleanEle)$")
public class gpx_towooz implements JvmHdl {

    protected XmlEntity<GpxFile> gpxEntity = new XmlEntityAnnotationMaker().makeEntity(null, GpxFile.class);
    
    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        String text = null;
        if (sys.pipeId > 0) {
            text = sys.in.readAll();
        }
        else {
            text = sys.io.readText(sys.io.check(null, Wn.normalizeFullPath(hc.params.val_check(0), sys)));
        }
        ByteArrayInputStream ins = new ByteArrayInputStream(text.getBytes());
        Element ele = Xmls.xml(ins).getDocumentElement();
        GpxFile gpx = gpxEntity.read(ele);
        String conv_from = hc.params.get("conv_from", "");
        String conv_to = hc.params.get("conv_to", "");
        boolean clean = hc.params.is("cleanEle"); // 清理ele不存在的记录
        WoozMap wooz = new WoozMap();
        if (gpx.wpts != null) {
            wooz.points = new ArrayList<>();
            int index = 0;
            for (GpxWpt wpt : gpx.wpts) {
                WoozPoint point = new WoozPoint();
                point.lat = Double.parseDouble(wpt.getLat());
                point.lng = Double.parseDouble(wpt.getLon());
                point.name = wpt.getName();
                // 暂时无法区分起点和终点
                point.type = "csp";
                if (index == 0) {
                    point.type = "start";
                }
                else if (index == gpx.wpts.size() - 1) {
                    point.type = "end";
                }
                index ++;
                WoozTools.convert(point, conv_from, conv_to);
                wooz.points.add(point);
            }
        }
        if (gpx.trk != null && gpx.trk.trkseg != null && gpx.trk.trkseg.trkpts != null) {
            wooz.route = new ArrayList<>();
            for (GpxTrkpt trkpt : gpx.trk.trkseg.trkpts) {
                if (clean && Strings.isBlank(trkpt.ele))
                    continue;
                WoozRoute route = new WoozRoute();
                route.lat = Double.parseDouble(trkpt.lat);
                route.lng = Double.parseDouble(trkpt.lon);
                if (trkpt.ele != null)
                    route.ele = Double.parseDouble(trkpt.ele);
                if (Strings.isNotBlank(trkpt.time)) {
                    // 2018-06-17T13:14:15Z
                    route.time = Times.parse("yyyy-MM-dd'T'HH:mm:ss'Z'", trkpt.time);
                }
                WoozTools.convert(route, conv_from, conv_to);
                wooz.route.add(route);
            }
        }
        sys.out.writeJson(wooz, Cmds.gen_json_format(hc.params));
    }
}