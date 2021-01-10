package org.nutz.walnut.alg.exp.val;

import org.nutz.walnut.alg.exp.WnExpValue;

public class WnExpValFloat implements WnExpValue {

    private float value;

    public WnExpValFloat(String v) {
        this.value = Float.parseFloat(v);
    }

    public WnExpValFloat(float v) {
        this.value = v;
    }

    public String toString() {
        return Float.toString(value);
    }
}
