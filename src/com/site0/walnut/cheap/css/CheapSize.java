package com.site0.walnut.cheap.css;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.site0.walnut.ooml.measure.bean.OomlMeaUnit;
import com.site0.walnut.ooml.measure.bean.OomlMeasure;
import com.site0.walnut.util.Ws;

public class CheapSize {

    private static Pattern SZ_P = Pattern.compile("^(-?[.\\d]+)(px|pt|rem|%)?$");

    private double value;

    private String unit;

    public CheapSize() {}

    public CheapSize(double value, String unit) {
        this.value = value;
        this.unit = unit;
    }

    public CheapSize(String size) {
        Matcher m = SZ_P.matcher(size);
        if (m.find()) {
            this.value = Double.parseDouble(m.group(1));
            this.unit = m.group(2);
        }
    }

    public String toString() {
        if (Ws.isBlank(unit)) {
            return Double.toString(value);
        }
        return value + unit;
    }

    public OomlMeasure toMeasure() {
        if ("pt".equals(unit)) {
            return OomlMeasure.PT(value);
        }
        if ("%".equals(unit)) {
            return OomlMeasure.PCT(value * 50);
        }
        if ("rem".equals(unit)) {
            return OomlMeasure.PX(value * 100);
        }
        return OomlMeasure.PX(value);
    }

    public int toDXA(OomlMeasure base, String osName) {
        OomlMeasure mea = this.toMeasure();
        mea.as(OomlMeaUnit.DXA, base, osName);
        return mea.getInt();
    }

    public int getIntValue() {
        return (int) Math.round(value);
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

}
