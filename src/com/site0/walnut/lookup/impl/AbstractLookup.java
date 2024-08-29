package com.site0.walnut.lookup.impl;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.lookup.WnLookup;
import com.site0.walnut.lookup.bean.LookupConfig;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;

public abstract class AbstractLookup implements WnLookup {

    private LookupConfig config;

    protected AbstractLookup(LookupConfig config) {
        this.config = config;
    }

    protected NutBean prepareHintForQuery(String hint) {
        String sep = config.getPartsSep();
        String[] parts = config.getQueryParts();
        return _prepare_hint(hint, "hint", sep, parts);
    }

    protected NutBean prepareHintForFetch(String hint) {
        String sep = config.getPartsSep();
        String[] parts = config.getFetchParts();
        return _prepare_hint(hint, "id", sep, parts);
    }

    static NutBean _prepare_hint(String hint, String hintKey, String sep, String[] parts) {
        NutMap re = new NutMap();
        // 尝试具名 Hint
        if (!Ws.isBlank(sep)) {
            String[] ss = Ws.splitIgnoreBlank(hint, sep);
            if (ss.length > 0) {
                re.put(hintKey, hint);
                for (int i = 0; i < ss.length; i++) {
                    String key;
                    Object val = ss[i];
                    // 指定了 key
                    if (null != parts && i < parts.length) {
                        key = parts[i];
                    }
                    // 填充一个 key
                    else {
                        key = hintKey + "_" + i;
                    }
                    // 二次渲染的key
                    int pos = key.indexOf(':');
                    if (pos > 0) {
                        String input = key.substring(pos + 1).trim();
                        key = key.substring(0, pos).trim();
                        NutMap ec = Wlang.map(key, val);
                        val = Wn.explainObj(ec, input);
                    }

                    // 计入返回上下文
                    re.put(key, val);
                }
            }
        }
        // name就用原始的 Hint
        else {
            re.put(hintKey, hint);
        }
        return re;
    }
}
