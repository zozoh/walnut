package org.nutz.lang.util;

public class StrRegion extends Region<String> {

    public StrRegion() {
        super();
    }

    public StrRegion(String str) {
        super();
        this.valueOf(str);
    }

    public String fromString(String str) {
        return str;
    }

    public String toString(String v) {
        return v;
    }
}
