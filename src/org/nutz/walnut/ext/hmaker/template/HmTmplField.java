package org.nutz.walnut.ext.hmaker.template;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;

public class HmTmplField {

    public static HmTmplField eval(Object obj) {
        if (obj instanceof HmTmplField)
            return (HmTmplField) obj;
        return new HmTmplField().valueBy(obj);
    }

    public String type;

    public String arg;

    public String dft;

    public NutMap mapping;

    public boolean required;

    public String key;

    public String title;

    public String tip;

    public HmTmplField valueOf(NutMap map) {
        type = map.getString("type", "input");
        arg = map.getString("arg");
        dft = map.getString("dft");
        mapping = map.getAs("mapping", NutMap.class);
        required = map.getBoolean("required");
        key = map.getString("key");
        title = map.getString("title");
        tip = map.getString("tip");
        return this;
    }

    public boolean isType(String type) {
        if (null != type && null != this.type)
            return type.equals(this.type);
        return false;
    }

    private static final Pattern P = Pattern.compile("^([*])?(\\(([^\\)]+)\\))?@(input|thingset|site|com|link)(=([^:#{]*))?(:([^#{]*))?(\\{[^}]*\\})?(#(.*))?$");

    public HmTmplField valueOf(String str) {
        // 重置
        reset();

        // 防止空
        if (null == str)
            return this;

        // 来吧
        Matcher m = P.matcher(str);

        if (m.find()) {
            required = "*".equals(m.group(1));
            title = m.group(3);
            type = m.group(4);
            dft = m.group(6);
            arg = m.group(8);
            String json = m.group(9);
            if (!Strings.isBlank(json))
                mapping = Json.fromJson(NutMap.class, json);
            tip = m.group(11);
        }

        return this;
    }

    @SuppressWarnings("unchecked")
    public HmTmplField valueBy(Object obj) {
        if (obj instanceof Map<?, ?>) {
            return this.valueOf(NutMap.WRAP((Map<String, Object>) obj));
        }
        return this.valueOf(obj.toString());
    }

    public HmTmplField reset() {
        key = null;
        type = "input";
        arg = null;
        mapping = null;
        required = false;
        title = null;
        tip = null;
        return this;
    }

    public HmTmplField setKey(String key) {
        this.key = key;
        return this;
    }

}
