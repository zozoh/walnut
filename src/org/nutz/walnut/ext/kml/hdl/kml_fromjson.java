package org.nutz.walnut.ext.kml.hdl;

import org.nutz.json.Json;
import org.nutz.plugins.xmlbind.entity.XmlEntity;
import org.nutz.plugins.xmlbind.entity.XmlEntityAnnotationMaker;
import org.nutz.walnut.ext.kml.bean.KmlFile;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

@JvmHdlParamArgs("cqn")
public class kml_fromjson implements JvmHdl {
    
    protected XmlEntity<KmlFile> kmlEntity = new XmlEntityAnnotationMaker().makeEntity(null, KmlFile.class);

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        String text = null;
        if (sys.pipeId > 0) {
            text = sys.in.readAll();
        }
        else {
            text = sys.io.readText(sys.io.check(null, Wn.normalizeFullPath(hc.params.val_check(0), sys)));
        }
        KmlFile gpx = Json.fromJson(KmlFile.class, text);
        sys.out.print(kmlEntity.write(gpx, "Doument"));
    }

}
