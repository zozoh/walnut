package org.nutz.walnut.cheap.css;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CheapSize {

    private static Pattern SZ_P = Pattern.compile("^([.\\d]+)(px|pt|rem|em|%)?$");

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
