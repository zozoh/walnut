package com.site0.walnut.ext.data.titanium.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.util.NutMap;
import com.site0.walnut.util.Ws;

public class TiDict {

    private String name;

    private Object data;

    private String value;

    private String text;

    public String getName() {
        return name;
    }

    public TiDict() {
        this.value = "value";
        this.text = "text";
    }

    public TiDict(List<NutMap> data) {
        this();
        this.data = data;
    }

    @SuppressWarnings("unchecked")
    public TiDict(Collection<?> data) {
        this();
        if (null != data && data.size() > 0) {
            List<NutMap> list = new ArrayList<>(data.size());
            for (Object it : data) {
                if (it instanceof Map) {
                    NutMap ito = NutMap.WRAP((Map<String, Object>) it);
                    list.add(ito);
                }
            }
            this.data = list;
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDataAsList() {
        return null != data && (data instanceof Collection<?>);
    }

    public boolean isDataAsDynamic() {
        return null != data && (data instanceof String);
    }

    public boolean isDataAsThing() {
        if (!this.isDataAsDynamic()) {
            return false;
        }
        String cmd = data.toString();
        return cmd.startsWith("thing ");
    }

    public boolean isDataAsJsonFile() {
        if (!this.isDataAsDynamic()) {
            return false;
        }
        String cmd = data.toString();
        return cmd.startsWith("cat ");
    }

    private static final Pattern P = Pattern.compile("^(thing|cat)\\s+([^ ]+).*$");

    public String getDynamicDataPath() {
        if (!this.isDataAsDynamic()) {
            return null;
        }
        String cmd = data.toString().trim();

        Matcher m = P.matcher(cmd);
        if (m.find()) {
            return m.group(2).trim();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public NutMap getMappingText2Value() {
        if (!this.isDataAsList()) {
            return null;
        }
        NutMap re = new NutMap();
        for (Object it : (Collection<?>) this.data) {
            NutMap ito = NutMap.WRAP((Map<String, Object>) it);
            Object from = ito.getOr(this.text);
            Object to = ito.getOr(this.value);
            if (null != from) {
                re.put(from.toString(), to);
            }
        }
        return re;
    }

    @SuppressWarnings("unchecked")
    public NutMap getMappingValue2Text() {
        if (!this.isDataAsList()) {
            return null;
        }
        NutMap re = new NutMap();
        for (Object it : (Collection<?>) this.data) {
            NutMap ito = NutMap.WRAP((Map<String, Object>) it);
            Object from = ito.getOr(this.value);
            Object to = ito.getOr(this.text);
            if (null != from) {
                re.put(from.toString(), to);
            }
        }
        return re;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getValueName() {
        String[] ss = Ws.splitIgnoreBlank(value, "[|]");
        return ss[0];
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getTextName() {
        String[] ss = Ws.splitIgnoreBlank(text, "[|]");
        return ss[0];
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}
