package org.nutz.walnut.ext.geo.gpx.hdl;

import java.io.ByteArrayInputStream;

import org.nutz.lang.Xmls;
import org.nutz.plugins.xmlbind.entity.XmlEntity;
import org.nutz.plugins.xmlbind.entity.XmlEntityAnnotationMaker;
import org.nutz.walnut.ext.geo.gpx.bean.GpxFile;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wn;
import org.w3c.dom.Element;

@JvmHdlParamArgs("cqn")
public class gpx_tojson implements JvmHdl {
    
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
        sys.out.writeJson(gpx, Cmds.gen_json_format(hc.params));
    }

}
