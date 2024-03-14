package com.site0.walnut.ext.media.ooml.explain.bean;

import java.util.Collection;
import java.util.Map;

import org.nutz.lang.util.NutBean;
import org.nutz.mapl.Mapl;
import com.site0.walnut.cheap.dom.CheapElement;
import com.site0.walnut.util.Ws;

public class OELoop extends OENode {

    private String keyName;

    private String loopWith;

    private String varName;

    public OELoop() {
        this.type = OENodeType.LOOP;
    }

    public String toBrief() {
        String s = super.toBrief();
        return String.format("%s : $<%s> = %s", s, loopWith, varName);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public CheapElement renderTo(CheapElement pEl, NutBean vars) {
        Object list = Mapl.cell(vars, loopWith);
        // 防空
        if (null == list || !hasChildren()) {
            return pEl;
        }

        Object old = vars.get(varName);
        try {
            // 集合
            if (list instanceof Collection<?>) {
                int i = 0;
                for (Object li : (Collection<?>) list) {
                    if (!Ws.isBlank(varName)) {
                        vars.put(varName, li);
                    }
                    if (!Ws.isBlank(keyName)) {
                        vars.put(keyName, i + 1);
                    }
                    for (OEItem it : children) {
                        it.renderTo(pEl, vars);
                    }
                    i++;
                }
            }
            // Map
            if (list instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) list;
                for (Map.Entry en : map.entrySet()) {
                    String key = en.getKey().toString();
                    Object val = en.getValue();
                    if (!Ws.isBlank(varName)) {
                        vars.put(varName, val);
                    }
                    if (!Ws.isBlank(keyName)) {
                        vars.put(keyName, key);
                    }
                    for (OEItem it : children) {
                        it.renderTo(pEl, vars);
                    }
                }
            }
        }
        // 恢复之前旧值
        finally {
            if (!Ws.isBlank(varName)) {
                vars.put(varName, old);
            }
        }

        return pEl;
    }

    public void setVarName(String varName) {
        this.varName = Ws.trim(varName);
    }

    public String getVarName() {
        return varName;
    }

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = Ws.trim(keyName);
    }

    public String getLoopWith() {
        return loopWith;
    }

    public void setLoopWith(String listName) {
        this.loopWith = Ws.trim(listName);
    }

}
