package com.site0.walnut.ext.sys.datex.hdl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.ext.sys.datex.DatexContext;
import com.site0.walnut.ext.sys.datex.DatexFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

public class datex_timezone extends DatexFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "Ncqn", "^(view|list)$");
    }

    private void output(WnSystem sys, boolean notNewLine, String str) {
        if (notNewLine) {
            sys.out.print(str);
        } else {
            sys.out.println(str);
        }
    }

    @Override
    protected void process(WnSystem sys, DatexContext fc, ZParams params) {
        String tzId = params.val(0);
        String as = params.getString("as");
        boolean hasTzId = !Ws.isBlank(tzId);
        boolean forView = !Ws.isBlank(as);
        boolean notNewLine = params.is("N");
        boolean showList = params.is("list");

        JsonFormat jfmt = Cmds.gen_json_format(params);

        // 显示所有可用的时区列表
        if (showList) {
            fc.quiet = true;
            String[] tzIds = TimeZone.getAvailableIDs();
            List<Object> list = new ArrayList<>(tzIds.length);
            for (int i = 0; i < tzIds.length; i++) {
                tzId = tzIds[i];
                TimeZone tz = TimeZone.getTimeZone(tzId);
                list.add(toResult(tzId, tz, as, "[GMT%s] %35s : %40s : %s"));
            }

            // 输出:JSON
            if ("json".equals(as)) {
                String str = Json.toJson(list, jfmt);
                output(sys, notNewLine, str);
            }
            // 输出:字符串
            else {
                for (int i = 0; i < list.size(); i++) {
                    Object li = list.get(i);
                    sys.out.printlnf("%03d) %s", i + 1, li);
                }
            }

            return;
        }

        // 设置 timezone
        if (hasTzId) {
            fc.displayTimeZone = TimeZone.getTimeZone(tzId);
        }
        // 显示 timezone
        if (!hasTzId || forView) {
            fc.quiet = true;
            TimeZone tz = fc.displayTimeZone;
            if (null == tz) {
                tz = Wn.WC().getTimeZone();
            }
            Object result = toResult(tzId, tz, as, "[GMT%s] %s : %s : %s");

            // 变成输出字符串
            String str;
            if (result instanceof String) {
                str = result.toString();
            } else {

                str = Json.toJson(result, jfmt);
            }

            // 输出字符串
            output(sys, notNewLine, str);
        }

    }

    private Object toResult(String tzId, TimeZone tz, String as, String str_fmt) {
        if (null == tz) {
            return tzId;
        }

        // 显示 ID
        if ("id".equals(as)) {
            return tz.getID();
        }

        // 显示名称
        if ("name".equals(as)) {
            return tz.getDisplayName();
        }

        // 计算一下小时偏移量
        int offset = tz.getRawOffset() / 3600000;

        // 显示 JSON
        if ("json".equals(as)) {
            NutMap bean = new NutMap();
            bean.put("id", tzId);
            bean.put("offset", offset);
            bean.put("text_cn", tz.getDisplayName(Locale.CHINESE));
            bean.put("text_en", tz.getDisplayName(Locale.ENGLISH));
            return bean;
        }
        // 显示完整描述
        String offset_in_str = offset < 0 ? String.valueOf(offset) : "+" + offset;
        return String.format(str_fmt,
                             Ws.padEnd(offset_in_str, 3, ' '),
                             tzId,
                             tz.getDisplayName(Locale.ENGLISH),
                             tz.getDisplayName(Locale.CHINESE));

    }

}