package org.nutz.walnut.ooml.measure.bean;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.ooml.Oomls;
import org.nutz.walnut.util.Ws;

public class OomlMeasure {

    private double value;

    private OomlMeaUnit unit;

    public OomlMeasure(double value) {
        this(value, null);
    }

    public OomlMeasure(double value, OomlMeaUnit unit) {
        this.value = value;
        this.unit = null == unit ? OomlMeaUnit.HPT : unit;
    }

    public OomlMeasure(String input) {
        this.parse(input);
    }

    private static Pattern P = Pattern.compile("^([0-9]+)(pt|px|dxa|inch|cm|emus|pct)?$");

    public OomlMeasure clone() {
        return new OomlMeasure(this.value, this.unit);
    }

    public String toString() {
        long v = Math.round(value);
        String u = unit.toString().toLowerCase();
        return v + u;
    }

    public OomlMeasure parse(String input) {
        String s = Ws.trim(input).toLowerCase();
        Matcher m = P.matcher(s);
        if (!m.find()) {
            throw Er.create("e.measure.InvalidFormat", s);
        }
        this.value = Double.parseDouble(m.group(1));
        String u = m.group(2);
        if (Ws.isBlank(u)) {
            this.unit = OomlMeaUnit.HPT;
        } else {
            this.unit = OomlMeaUnit.valueOf(u);
            ;
        }
        return this;
    }

    public OomlMeasure as(OomlMeaUnit unit, String os) {
        if (this.unit != unit) {
            // 防守：百分比
            if (OomlMeaUnit.PCT == unit || OomlMeaUnit.PCT == this.unit) {
                throw Er.create("e.measure.ConvertPCTWithoutBase");
            }
            // 平台
            if (!"win".equals(os) && !"mac".equals(os)) {
                throw Er.create("e.measure.UnknownOS", os);
            }
            double v0;
            String from;
            // 半点
            if (OomlMeaUnit.HPT == this.unit) {
                v0 = this.value / 2;
                from = "pt";
            }
            // 其他
            else {
                v0 = this.value;
                from = this.unit.name().toLowerCase();
            }
            String name = String.format("%s_%s", from, unit.name().toLowerCase());
            // 像素需要平台前缀
            if (OomlMeaUnit.PX == unit || OomlMeaUnit.PX == this.unit) {
                name = os + "_" + name;
            }
            double v1 = Oomls.convertMeasure(name, v0);
            this.value = v1;
            this.unit = unit;
        }
        return this;
    }

    public OomlMeasure as(OomlMeaUnit unit, OomlMeasure base) {
        if (this.unit != unit) {
            // 防守:像素
            if (OomlMeaUnit.PX == unit || OomlMeaUnit.PX == this.unit) {
                throw Er.create("e.measure.ConvertPXWithoutOS");
            }
            // 转换至百分比
            if (OomlMeaUnit.PCT == unit) {
                // 防守(除 0)
                if (base.value == 0) {
                    throw Er.create("e.measure.ConvertPCTByZeroBase", base.toString());
                }
                this.as(base.unit);
                double pct = this.value / base.value;
                this.value = pct * 50;
                this.unit = OomlMeaUnit.PCT;
                return this;
            }
            // 百分比: from
            if (OomlMeaUnit.PCT == this.unit) {
                double pct = this.value / 50;
                this.value = base.value * pct;
                this.unit = base.unit;
            }
            // 安全转换
            __safe_as(unit);
        }
        return this;
    }

    public OomlMeasure as(OomlMeaUnit unit) {
        if (this.unit != unit) {
            // 防守：百分比
            if (OomlMeaUnit.PCT == unit || OomlMeaUnit.PCT == this.unit) {
                throw Er.create("e.measure.ConvertPCTWithoutBase");
            }
            // 防守:像素
            if (OomlMeaUnit.PX == unit || OomlMeaUnit.PX == this.unit) {
                throw Er.create("e.measure.ConvertPXWithoutOS");
            }
            // 安全转换
            __safe_as(unit);
        }
        return this;
    }

    private void __safe_as(OomlMeaUnit unit) {
        double v0;
        String from;
        // 半点
        if (OomlMeaUnit.HPT == this.unit) {
            v0 = this.value / 2;
            from = "pt";
        }
        // 其他
        else {
            v0 = this.value;
            from = this.unit.name().toLowerCase();
        }
        String name = String.format("%s_%s", from, unit.name().toLowerCase());
        double v1 = Oomls.convertMeasure(name, v0);
        this.value = v1;
        this.unit = unit;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public OomlMeaUnit getUnit() {
        return unit;
    }

    public void setUnit(OomlMeaUnit unit) {
        this.unit = unit;
    }

}
