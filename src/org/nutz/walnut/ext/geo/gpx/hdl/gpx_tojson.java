package org.nutz.walnut.ext.geo.gpx.hdl;

import org.nutz.plugins.xmlbind.entity.XmlEntity;
import org.nutz.plugins.xmlbind.entity.XmlEntityAnnotationMaker;
import org.nutz.walnut.ext.geo.gpx.bean.GpxFile;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;

@JvmHdlParamArgs("cqn")
public class gpx_tojson implements JvmHdl {

    protected XmlEntity<GpxFile> gpxEntity = new XmlEntityAnnotationMaker().makeEntity(null,
                                                                                       GpxFile.class);

    // TODO 以后采用 CheapDocument 实现
    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // String text = null;
        // if (sys.pipeId > 0) {
        // text = sys.in.readAll();
        // } else {
        // text = sys.io.readText(sys.io.check(null,
        // Wn.normalizeFullPath(hc.params.val_check(0), sys)));
        // }
        // ByteArrayInputStream ins = new ByteArrayInputStream(text.getBytes());

        // Element ele = Xmls.xml(ins).getDocumentElement();
        // GpxFile gpx = gpxEntity.read(ele);
        // sys.out.writeJson(gpx, Cmds.gen_json_format(hc.params));
    }

}
