package org.nutz.walnut.ext.data.fake;

import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.Wuu;

public class WnFakeWord {

    private String[] data;

    public WnFakeWord(String[] data) {
        this.data = data;
    }

    public WnFakeWord(String input) {
        this.data = Ws.splitIgnoreBlank(input, "[\\s,;]+");
    }

    public String next() {
        if (null == data || data.length == 0) {
            return null;
        }
        if (1 == data.length) {
            return data[0];
        }
        int i = Wuu.random(0, data.length);
        return data[i];
    }

}
