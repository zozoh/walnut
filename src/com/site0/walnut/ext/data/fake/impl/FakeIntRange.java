package com.site0.walnut.ext.data.fake.impl;

import com.site0.walnut.util.Wuu;

public class FakeIntRange {

    protected int min;

    protected int max;

    public FakeIntRange(String input) {
        this(input, 10);
    }

    protected FakeIntRange(String input, int radix) {
        int pos = input.indexOf('-');
        if (pos > 0) {
            int min = Integer.parseInt(input.substring(0, pos).trim(), radix);
            int max = Integer.parseInt(input.substring(pos + 1).trim(), radix);
            _tidy(min, max);
        }
        // 如果只有一个值，则表示数值
        else {
            int n = Integer.parseInt(input);
            _tidy(n, n);
        }
    }

    public FakeIntRange(int min, int max) {
        _tidy(min, max);
    }

    protected void _tidy(int min, int max) {
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
