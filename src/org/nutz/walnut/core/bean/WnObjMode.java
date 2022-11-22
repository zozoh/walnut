package org.nutz.walnut.core.bean;

import org.nutz.walnut.util.Wn;

public class WnObjMode {
    
    public static WnObjMode parse(Object input) {
        return new WnObjMode(input);
    }

    private WnObjModeType type;

    private int value;

    public WnObjMode() {}

    public WnObjMode(Object md) {
        this.parser(md);
    }

    public WnObjMode parser(Object md) {
        if (null == md) {
            this.type = WnObjModeType.DEFAULT;
            this.value = 0;
        }
        if (md instanceof CharSequence) {
            String s = md.toString();
            // 强混合
            if (s.startsWith("!")) {
                this.type = WnObjModeType.STRONG;
                s = s.substring(1);
            }
            // 弱混合
            else if (s.startsWith("~")) {
                this.type = WnObjModeType.WEAK;
                s = s.substring(1);
            }
            // 默认混合
            else {
                this.type = WnObjModeType.DEFAULT;
            }
            this.value = Wn.Io.modeFromStr(s);
        }
        else if (md instanceof Number) {
            int m = ((Number) md).intValue();
            if (m < 0) {
                this.type = WnObjModeType.WEAK;
                this.value = Math.abs(m);
            } else {
                this.type = WnObjModeType.DEFAULT;
                this.value = m;
            }
        }
        else {
            this.type = WnObjModeType.DEFAULT;
            this.value = Wn.Io.modeFrom(md, 0);
        }

        return this;
    }

    public boolean isDefault() {
        return isType(WnObjModeType.DEFAULT);
    }

    public boolean isWeak() {
        return isType(WnObjModeType.WEAK);
    }

    public boolean isStrong() {
        return isType(WnObjModeType.STRONG);
    }

    public boolean isType(WnObjModeType type) {
        return this.type == type;
    }

    public WnObjModeType getType() {
        return type;
    }

    public void setType(WnObjModeType type) {
        this.type = type;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

}
