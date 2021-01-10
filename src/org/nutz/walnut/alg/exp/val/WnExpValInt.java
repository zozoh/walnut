package org.nutz.walnut.alg.exp.val;

import org.nutz.walnut.alg.exp.WnExpValue;

public class WnExpValInt implements WnExpValue {

    private int value;
    
    public WnExpValInt(String v) {
        this.value = Integer.parseInt(v);
    }

    public WnExpValInt(int v) {
        this.value = v;
    }
    
    public String toString() {
        return Integer.toString(value);
    }

}
