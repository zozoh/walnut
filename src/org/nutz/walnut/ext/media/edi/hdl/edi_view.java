package org.nutz.walnut.ext.media.edi.hdl;

import org.nutz.walnut.ext.media.edi.EdiContext;
import org.nutz.walnut.ext.media.edi.EdiFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class edi_view extends EdiFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqn");
    }

    @Override
    protected void process(WnSystem sys, EdiContext fc, ZParams params) {
        fc.assertIC();

        String as = params.getString("as", "json");

        // 作为 JSON 输出
        if ("tree".equals(as)) {

        }
        // 输出上下文文本
        else if ("text".equals(as)) {
            sys.out.println(fc.message);
        }
        // 作为树输出
        else {

        }
    }

}
