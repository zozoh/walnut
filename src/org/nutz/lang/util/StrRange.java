package org.nutz.lang.util;

public class StrRange extends ValueRange<String> {

    public StrRange() {
        super();
    }

    public StrRange(String str) {
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
