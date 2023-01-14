package org.nutz.walnut.ext.data.fake.impl;

import org.nutz.walnut.util.Wuu;

public class FakeIntRange {

    protected int min;

    protected int max;

    public FakeIntRange(String input) {
        int pos = input.indexOf('-');
        if (pos > 0) {
            int min = Integer.parseInt(input.substring(0, pos).trim());
            int max = Integer.parseInt(input.substring(pos + 1).trim());
            __tidy(min, max);
        }
        // 如果只有一个值，则表示数值
        else {
            int n = Integer.parseInt(input);
            __tidy(n, n);
        }
    }

    public FakeIntRange(int min, int max) {
        __tidy(min, max);
    }

    private void __tidy(int min, int max) {
        if (min > max) {
            this.min = max;
            this.max = min;
        } else {
            this.min = min;
            this.max = max;
        }
    }

    public int randomN() {
        if (max > min) {
            return Wuu.random(min, max);
        }
        return min;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

}
