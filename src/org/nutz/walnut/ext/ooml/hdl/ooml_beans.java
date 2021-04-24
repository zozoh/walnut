package org.nutz.walnut.ext.ooml.hdl;

import java.util.List;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutBean;
import org.nutz.walnut.ext.ooml.OomlContext;
import org.nutz.walnut.ext.ooml.OomlFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.ZParams;

public class ooml_beans extends OomlFilter {

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

        // 默认第一行作为标题行
        int headIndex = params.getInt("head", 0);

        // 将行转换为对象
        Map<String, String> header = fc.sheet.getHeaderMapping(headIndex);
        List<NutBean> beans = fc.sheet.toBeans(header, headIndex + 1);

        // 转换Bean的键
        beans = fc.tranlateBeans(beans);

        // 输出 JSON
        JsonFormat jfmt = Cmds.gen_json_format(params);
        String json = Json.toJson(beans, jfmt);
        sys.out.println(json);
    }

}
