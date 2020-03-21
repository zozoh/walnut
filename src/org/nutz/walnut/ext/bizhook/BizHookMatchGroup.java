package org.nutz.walnut.ext.bizhook;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

/**
 * 匹配组。必须全部匹配才算匹配上。
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class BizHookMatchGroup {

    private List<BizHookMatch> matchs;

    public BizHookMatchGroup(NutMap map) {
        this.matchs = new ArrayList<>(map.size());
        for (Map.Entry<String, Object> en : map.entrySet()) {
            String key = en.getKey();
            Object val = en.getValue();
            BizHookMatch bhm = new BizHookMatch(key, val);
            this.matchs.add(bhm);
        }
    }

    public boolean match(NutBean obj) {
        for (BizHookMatch bhm : matchs) {
            if (!bhm.match(obj))
                return false;
        }
        return true;
    }

}
