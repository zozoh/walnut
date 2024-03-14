package com.site0.walnut.cheap.css;

import java.util.Map;

import org.nutz.lang.util.NutMap;
import com.site0.walnut.util.Ws;

public class CheapStyle extends NutMap {

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> en : this.entrySet()) {
            String key = en.getKey();
            Object val = en.getValue();
            if (null != val) {
                String name = Ws.kebabCase(key);
                sb.append(name).append(':');
                sb.append(val.toString()).append(';');
            }
        }
        return sb.toString();
    }

    public boolean isMatch(String name, String val) {
        String str = this.getString(name, null);
        if (null == str || null == val) {
            return false;
        }
        return str.equalsIgnoreCase(val);
    }

    public CheapSize getSize(String name) {
        return getSize(name, null);
    }

    public CheapSize getSize(String name, String dft) {
        String str = this.getString(name, dft);
        if (Ws.isBlank(str))
            return null;
        return new CheapSize(str);
    }

    @Override
    public Object put(String name, Object val) {
        String k = Ws.kebabCase(name);
        String c = Ws.camelCase(name);
        if (this.containsKey(k)) {
            super.put(k, val);
        } else if (this.containsKey(c)) {
            super.put(c, val);
        } else {
            super.put(name, val);
        }

        return val;
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> m) {
        for (Map.Entry<? extends String, ? extends Object> en : m.entrySet()) {
            String key = en.getKey();
            Object val = en.getValue();
            this.put(key, val);
        }
    }

}
