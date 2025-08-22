package com.site0.walnut.core.bm;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Ws;

public class Sha1Parts {

    private int[] parts;

    public Sha1Parts(String parts) {
        // 如何分段, parts=22 表示 [2,2]
        // 譬如签名: b10d47941e27dad21b63fb76443e1669195328f2
        // 对应路径: b1/0d/47941e27dad21b63fb76443e1669195328f2
        if (Ws.isBlank(parts)) {
            this.parts = new int[0];
        } else if (parts.matches("^[1-9]+$")) {
            char[] cs = parts.toCharArray();
            this.parts = new int[cs.length];
            for (int i = 0; i < cs.length; i++) {
                this.parts[i] = cs[i] - '0';
            }
        } else {
            throw Er.create("e.io.bm.invalidParts", parts);
        }
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (null == obj)
            return false;
        if (obj instanceof Sha1Parts) {
            Sha1Parts ta = (Sha1Parts) obj;
            return Wlang.isEqual(parts, ta.parts);
        }
        return false;
    }

    public String toPath(String sha1) {
        String[] ss = new String[parts.length + 1];
        int i = 0;
        int pos = 0;
        for (; i < parts.length; i++) {
            int p = parts[i];
            ss[i] = sha1.substring(pos, pos + p);
            pos += p;
        }
        ss[i] = sha1.substring(pos);
        return Ws.join(ss, "/");
    }

    public String fromPath(String path) {
        return path.replace("/", "");
    }
}
