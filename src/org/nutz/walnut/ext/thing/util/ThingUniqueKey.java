package org.nutz.walnut.ext.thing.util;

import org.nutz.lang.Strings;

public class ThingUniqueKey {

    private String[] name;

    private boolean required;

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
        return String.format("%s%s", required ? "*" : "", Strings.join(",", name));
    }

}
