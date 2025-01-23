package com.site0.walnut.ext.sys.datex.hdl;

import java.util.TimeZone;

import com.site0.walnut.ext.sys.datex.DatexContext;
import com.site0.walnut.ext.sys.datex.DatexFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

public class datex_timezone extends DatexFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "n", "^(view)$");
    }

    @Override
    protected void process(WnSystem sys, DatexContext fc, ZParams params) {
        String tzId = params.val(0);
        boolean hasTzId = !Ws.isBlank(tzId);
        boolean forView = params.is("view");
        boolean notNewLine = params.is("n");
        String as = params.getString("as", "full");

        // 设置 timezone
        if (hasTzId) {
            fc.outputTimeZone = TimeZone.getTimeZone(tzId);
        }
        // 显示 timezone
        if (!hasTzId || forView) {
            fc.quiet = true;
            TimeZone tz = fc.outputTimeZone;
            if (null == tz) {
                tz = Wn.WC().getTimeZone();
            }
            String str;
            if ("id".equals(as)) {
                str = tz.getID();
            } else if ("name".equals(as)) {
                str = tz.getDisplayName();
            } else {
                str = tz.toString();
            }

            if (notNewLine) {
                sys.out.print(str);
            } else {
                sys.out.println(str);
            }
        }

    }

}