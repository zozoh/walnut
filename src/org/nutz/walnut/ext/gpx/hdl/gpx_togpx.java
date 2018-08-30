package org.nutz.walnut.ext.gpx.hdl;

import org.nutz.json.Json;
import org.nutz.plugins.xmlbind.entity.XmlEntity;
import org.nutz.plugins.xmlbind.entity.XmlEntityAnnotationMaker;
import org.nutz.walnut.ext.gpx.bean.GpxFile;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

@JvmHdlParamArgs("cqn")
public class gpx_togpx implements JvmHdl {
    
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
        GpxFile gpx = Json.fromJson(GpxFile.class, text);
        sys.out.print(gpxEntity.write(gpx, "gpx"));
    }

}
