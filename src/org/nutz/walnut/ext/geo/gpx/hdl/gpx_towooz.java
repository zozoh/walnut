package org.nutz.walnut.ext.geo.gpx.hdl;

import org.nutz.plugins.xmlbind.entity.XmlEntity;
import org.nutz.plugins.xmlbind.entity.XmlEntityAnnotationMaker;
import org.nutz.walnut.ext.geo.gpx.bean.GpxFile;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;

// TODO 以后采用 CheapDocument 实现
@JvmHdlParamArgs(value = "cqn", regex = "^(cleanEle)$")
public class gpx_towooz implements JvmHdl {

    protected XmlEntity<GpxFile> gpxEntity = new XmlEntityAnnotationMaker().makeEntity(null,
                                                                                       GpxFile.class);

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // String text = null;
        // if (sys.pipeId > 0) {
        // text = sys.in.readAll();
        // }
        // else {
        // text = sys.io.readText(sys.io.check(null,
        // Wn.normalizeFullPath(hc.params.val_check(0), sys)));
        // }
        // ByteArrayInputStream ins = new ByteArrayInputStream(text.getBytes());
        // Element ele = Xmls.xml(ins).getDocumentElement();
        // GpxFile gpx = gpxEntity.read(ele);
        // String conv_from = hc.params.get("conv_from", "");
        // String conv_to = hc.params.get("conv_to", "");
        // boolean clean = hc.params.is("cleanEle"); // 清理ele不存在的记录
        // WoozMap wooz = new WoozMap();
        // if (gpx.wpts != null) {
        // wooz.points = new ArrayList<>();
        // int index = 0;
        // for (GpxWpt wpt : gpx.wpts) {
        // WoozPoint point = new WoozPoint();
        // point.lat = Double.parseDouble(wpt.getLat());
        // point.lng = Double.parseDouble(wpt.getLon());
        // point.name = Strings.sBlank(wpt.getName(), "CP" + index);
        // if (Strings.isNotBlank(point.name)) {
        // if (point.name.toUpperCase().startsWith("CP")) {
        // point.type = "cp";
        // if (point.name.contains(" ")) {
        // point.name = point.name.substring(0, point.name.indexOf(' '));
        // point.desc = point.name.substring(point.name.indexOf(' ')+1);
        // }
        // else if (point.name.contains(":")) {
        // point.name = point.name.substring(0, point.name.indexOf(':'));
        // point.desc = point.name.substring(point.name.indexOf(':')+1);
        // if (point.name.equals("CP0")) {
        // point.type = "start";
        // }
        // }
        // }
        // else if (point.name.toUpperCase().startsWith("SP")) {
        // point.type = "sp";
        // if (point.name.contains(" ")) {
        // point.name = point.name.substring(0, point.name.indexOf(' '));
        // point.desc = point.name.substring(point.name.indexOf(' ')+1);
        // }
        // }
        // else {
        // if (index == 0) {
        // point.type = "start";
        // }
        // else if (index == gpx.wpts.size() - 1) {
        // point.type = "end";
        // }
        // else {
        // point.type = "sp";
        // }
        // }
        // }
        // index ++;
        // WoozTools.convert(point, conv_from, conv_to);
        // wooz.points.add(point);
        // }
        // }
        // if (gpx.trk != null && gpx.trk.trkseg != null &&
        // gpx.trk.trkseg.trkpts != null) {
        // wooz.route = new ArrayList<>();
        // for (GpxTrkpt trkpt : gpx.trk.trkseg.trkpts) {
        // if (clean && Strings.isBlank(trkpt.ele))
        // continue;
        // WoozRoute route = new WoozRoute();
        // route.lat = Double.parseDouble(trkpt.lat);
        // route.lng = Double.parseDouble(trkpt.lon);
        // if (trkpt.ele != null)
        // route.ele = Double.parseDouble(trkpt.ele);
        // if (Strings.isNotBlank(trkpt.time)) {
        // // 2018-06-17T13:14:15Z
        // route.time = Times.parse("yyyy-MM-dd'T'HH:mm:ss'Z'", trkpt.time);
        // }
        // WoozTools.convert(route, conv_from, conv_to);
        // wooz.route.add(route);
        // }
        // }
        // sys.out.writeJson(wooz, Cmds.gen_json_format(hc.params));
    }
}
