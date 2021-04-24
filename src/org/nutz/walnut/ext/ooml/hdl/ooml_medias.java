package org.nutz.walnut.ext.ooml.hdl;

import java.util.List;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.walnut.ext.ooml.OomlContext;
import org.nutz.walnut.ext.ooml.OomlFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.ooml.xlsx.XlsxMedia;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.ZParams;

public class ooml_medias extends OomlFilter {
    
    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqn");
    }

    @Override
    protected void process(WnSystem sys, OomlContext fc, ZParams params) {
        // 防守
        if (null == fc.sheet) {
            sys.out.println("[]");
            return;
        }

        // 得到行列表
        List<XlsxMedia> medias = fc.sheet.getMedias();

        // 输出 JSON
        JsonFormat jfmt = Cmds.gen_json_format(params);
        String json = Json.toJson(medias, jfmt);
        sys.out.println(json);
    }

}
