package com.site0.walnut.ext.media.ooml.hdl;

import java.util.List;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import com.site0.walnut.ext.media.ooml.OomlContext;
import com.site0.walnut.ext.media.ooml.OomlFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.ooml.xlsx.XlsxMedia;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.ZParams;

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
