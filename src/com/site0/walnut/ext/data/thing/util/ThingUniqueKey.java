package com.site0.walnut.ext.data.thing.util;

import com.site0.walnut.util.Ws;
import org.nutz.lang.util.NutBean;

public class ThingUniqueKey {

    private String[] name;

    private boolean required;

    public String getValueAsString(NutBean obj) {
        if (null == obj)
            return Ws.dup('?', name.length);
        String[] vals = new String[name.length];
        for (int i = 0; i < name.length; i++) {
            String v = obj.getString(name[i]);
            vals[i] = v;
        }
        return Ws.join(", ", vals);
    }

    public String[] getName() {
        return name;
    }

    public void setName(String[] name) {
        this.name = name;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String toString() {
        return String.format("%s%s", required ? "*" : "", Ws.join(",", name));
    }

    public String toString(NutBean obj) {
        return String.format("'%s' (%s%s)",
                             this.getValueAsString(obj),
                             required ? "*" : "",
                             Ws.join(",", name));
    }

}
