package org.nutz.walnut.ooml.measure.bean;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.ooml.Oomls;
import org.nutz.walnut.util.Ws;

public class OomlMeasure {

    /**
     * @return 度量单位
     */
    public static OomlMeasure create(String input) {
        return new OomlMeasure(input);
    }

    /**
     * @return 度量单位
     */
    public static OomlMeasure create(double v) {
        return new OomlMeasure(v, null);
    }

    /**
     * @return 度量单位
     */
    public static OomlMeasure create(double v, OomlMeaUnit unit) {
        return new OomlMeasure(v, unit);
    }

    /**
     * @return 磅点
     */
    public static OomlMeasure PT(double v) {
        return create(v, OomlMeaUnit.PT);
    }

    /**
     * @return 像素
     */
    public static OomlMeasure PX(double v) {
        return create(v, OomlMeaUnit.PX);
    }

    /**
     * @return 二十分点
     */
    public static OomlMeasure DXA(double v) {
        return create(v, OomlMeaUnit.DXA);
    }

    /**
     * @return 英寸
     */
    public static OomlMeasure INCH(double v) {
        return create(v, OomlMeaUnit.INCH);
    }

    /**
     * @return 厘米
     */
    public static OomlMeasure CM(double v) {
        return create(v, OomlMeaUnit.CM);
    }

    /**
     * @return 英米单位
     */
    public static OomlMeasure EMUS(double v) {
        return create(v, OomlMeaUnit.EMUS);
    }

    /**
     * @return 半点
     */
    public static OomlMeasure HPT(double v) {
        return create(v, OomlMeaUnit.HPT);
    }

    /**
     * @return 五十倍百分比
     */
    public static OomlMeasure PCT(double v) {
        return create(v, OomlMeaUnit.PCT);
    }

    private double value;

    private OomlMeaUnit unit;

    private OomlMeasure(double value, OomlMeaUnit unit) {
        this.value = value;
        this.unit = null == unit ? OomlMeaUnit.HPT : unit;
    }

    private OomlMeasure(String input) {
        this.parse(input);
    }

    public OomlMeasure sub(OomlMeasure mea) {
        if (this.unit != mea.unit) {
            throw Er.create("e.measure.sub.UnitUnMatched", String.format("%s - %s", this, mea));
        }
        return new OomlMeasure(this.value - mea.value, this.unit);
    }

    public OomlMeasure dif(OomlMeasure mea) {
        if (this.unit != mea.unit) {
            throw Er.create("e.measure.sub.UnitUnMatched",
                            String.format("ABS(%s - %s)", this, mea));
        }
        return new OomlMeasure(Math.abs(this.value - mea.value), this.unit);
    }

    public OomlMeasure add(OomlMeasure mea) {
        if (this.unit != mea.unit) {
            throw Er.create("e.measure.sub.UnitUnMatched", String.format("%s + %s", this, mea));
        }
        return new OomlMeasure(this.value + mea.value, this.unit);
    }

    public OomlMeasure div(double n) {
        return new OomlMeasure(this.value / n, this.unit);
    }

    public OomlMeasure mul(double n) {
        return new OomlMeasure(this.value * n, this.unit);
    }

    public int getPixel(OomlMeasure base, String osName) {
        OomlMeasure mea = this.clone();
        mea.as(OomlMeaUnit.PX, base, osName);
        return (int) Math.round(mea.getValue());
    }

    public double getValue() {
        return value;
    }

    public long getLong() {
        return Math.round(value);
    }

    public int getInt() {
        return (int) Math.round(value);
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

    public OomlMeasure as(OomlMeaUnit unit, OomlMeasure base, String os) {
        if (this.unit != unit) {
            // 防守：百分比
            if (OomlMeaUnit.PCT == unit || this.isPCT()) {
                if (null == base) {
                    throw Er.create("e.measure.ConvertPCTWithoutBase");
                }
                if (base.isPCT()) {
                    throw Er.create("e.measure.ConvertPCTBasePCT");
                }
            }
            // 防守:像素
            if (OomlMeaUnit.PX == unit || this.isPX()) {
                if (Ws.isBlank(os)) {
                    throw Er.create("e.measure.ConvertPXWithoutOS");
                }
                if (!"win".equals(os) && !"mac".equals(os)) {
                    throw Er.create("e.measure.UnknownOS", os);
                }
            }

            // 转换至百分比
            if (OomlMeaUnit.PCT == unit) {
                // 防守(除 0)
                if (base.value == 0) {
                    throw Er.create("e.measure.ConvertPCTByZeroBase", base.toString());
                }
                this.as(base.unit);
                double pct = this.value / base.value;
                this.value = pct * 5000;
                this.unit = OomlMeaUnit.PCT;
                return this;
            }
            // 百分比: from
            else if (this.isPCT()) {
                double pct = this.value / 5000;
                this.value = base.value * pct;
                this.unit = base.unit;
                // 后面再根据标准流程转换一遍
            }

            // 其他转换
            // 因为百分比会预先转换一遍单位，所以这里需要再次判断一下
            if (this.unit != unit) {
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
                // 得到转换目标
                String to = unit.name().toLowerCase();
                String name = String.format("%s_%s", from, to);
                // 像素需要平台前缀
                if (OomlMeaUnit.PX == unit || OomlMeaUnit.PX == this.unit) {
                    name = os + "_" + name;
                }
                double v1 = Oomls.convertMeasure(name, v0);
                this.value = v1;
                this.unit = unit;
            }
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

    /**
     * @return 新创建的磅点
     */
    public OomlMeasure asPT(OomlMeasure base, String os) {
        return this.as(OomlMeaUnit.PT, base, os);
    }

    /**
     * @return 新创建的像素
     */
    public OomlMeasure asPX(OomlMeasure base, String os) {
        return this.as(OomlMeaUnit.PX, base, os);
    }

    /**
     * @return 新创建的二十分点
     */
    public OomlMeasure asDXA(OomlMeasure base, String os) {
        return this.as(OomlMeaUnit.DXA, base, os);
    }

    /**
     * @return 新创建的英寸
     */
    public OomlMeasure asINCH(OomlMeasure base, String os) {
        return this.as(OomlMeaUnit.INCH, base, os);
    }

    /**
     * @return 新创建的厘米
     */
    public OomlMeasure asCM(OomlMeasure base, String os) {
        return this.as(OomlMeaUnit.CM, base, os);
    }

    /**
     * @return 新创建的英米单位
     */
    public OomlMeasure asEMUS(OomlMeasure base, String os) {
        return this.as(OomlMeaUnit.EMUS, base, os);
    }

    /**
     * @return 新创建的半点
     */
    public OomlMeasure asHPT(OomlMeasure base, String os) {
        return this.as(OomlMeaUnit.HPT, base, os);
    }

    /**
     * @return 新创建的五十倍百分比
     */
    public OomlMeasure asPCT(OomlMeasure base, String os) {
        return this.as(OomlMeaUnit.PCT, base, os);
    }

    public OomlMeasure to(OomlMeaUnit unit, OomlMeasure base, String os) {
        return this.clone().as(unit, base, os);
    }

    /**
     * @return 新创建的磅点
     */
    public OomlMeasure toPT(OomlMeasure base, String os) {
        return this.clone().as(OomlMeaUnit.PT, base, os);
    }

    /**
     * @return 新创建的像素
     */
    public OomlMeasure toPX(OomlMeasure base, String os) {
        return this.clone().as(OomlMeaUnit.PX, base, os);
    }

    /**
     * @return 新创建的二十分点
     */
    public OomlMeasure toDXA(OomlMeasure base, String os) {
        return this.clone().as(OomlMeaUnit.DXA, base, os);
    }

    /**
     * @return 新创建的英寸
     */
    public OomlMeasure toINCH(OomlMeasure base, String os) {
        return this.clone().as(OomlMeaUnit.INCH, base, os);
    }

    /**
     * @return 新创建的厘米
     */
    public OomlMeasure toCM(OomlMeasure base, String os) {
        return this.clone().as(OomlMeaUnit.CM, base, os);
    }

    /**
     * @return 新创建的英米单位
     */
    public OomlMeasure toEMUS(OomlMeasure base, String os) {
        return this.clone().as(OomlMeaUnit.EMUS, base, os);
    }

    /**
     * @return 新创建的半点
     */
    public OomlMeasure toHPT(OomlMeasure base, String os) {
        return this.clone().as(OomlMeaUnit.HPT, base, os);
    }

    /**
     * @return 新创建的五十倍百分比
     */
    public OomlMeasure toPCT(OomlMeasure base, String os) {
        return this.clone().as(OomlMeaUnit.PCT, base, os);
    }

    /**
     * @return 是否是指定单位
     */
    public boolean is(OomlMeaUnit unit) {
        return unit == this.unit;
    }

    /**
     * @return 单位是否是磅点
     */
    public boolean isPT() {
        return OomlMeaUnit.PT == this.unit;
    }

    /**
     * @return 单位是否是像素
     */
    public boolean isPX() {
        return OomlMeaUnit.PX == this.unit;
    }

    /**
     * @return 单位是否是二十分点
     */
    public boolean isDXA() {
        return OomlMeaUnit.DXA == this.unit;
    }

    /**
     * @return 单位是否是英寸
     */
    public boolean isINCH() {
        return OomlMeaUnit.INCH == this.unit;
    }

    /**
     * @return 单位是否是厘米
     */
    public boolean isCM() {
        return OomlMeaUnit.CM == this.unit;
    }

    /**
     * @return 单位是否是英米单位
     */
    public boolean isEMUS() {
        return OomlMeaUnit.EMUS == this.unit;
    }

    /**
     * @return 单位是否是半点
     */
    public boolean isHPT() {
        return OomlMeaUnit.HPT == this.unit;
    }

    /**
     * @return 单位是否是五十倍百分比
     */
    public boolean isPCT() {
        return OomlMeaUnit.PCT == this.unit;
    }
}
