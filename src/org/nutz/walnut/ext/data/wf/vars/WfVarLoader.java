package org.nutz.walnut.ext.data.wf.vars;

import java.util.Map;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.validate.WnMatch;

public abstract class WfVarLoader {

    private String varName;

    private WnMatch keyPicker;

    protected abstract NutBean getBean(NutBean vars);
    
    public WfVarLoader(String varName) {
        this.varName = varName;
    }

    public void loadTo(NutBean vars) {
        NutBean bean = this.getBean(vars);
        boolean hasVarName = !Ws.isBlank(varName);

        if (null == bean || bean.isEmpty()) {
            return;
        }
        if (null == keyPicker) {
            if (hasVarName) {
                vars.put(varName, bean);
            } else {
                vars.putAll(bean);
            }
        }
        // 挑选
        else {
            if (hasVarName) {
                NutMap vs = new NutMap();
                putAllVars(bean, vs);
                vars.put(varName, vs);
            }
            // 拆包，设置
            else {
                putAllVars(bean, vars);
            }
        }
    }

    private void putAllVars(NutBean bean, NutBean vs) {
        for (Map.Entry<String, Object> en : bean.entrySet()) {
            String key = en.getKey();
            if (keyPicker.match(key)) {
                Object val = en.getValue();
                vs.put(key, val);
            }
        }
    }

    public String getVarName() {
        return varName;
    }

    public void setVarName(String varName) {
        this.varName = varName;
    }

    public WnMatch getKeyPicker() {
        return keyPicker;
    }

    public void setKeyPicker(WnMatch mKey) {
        this.keyPicker = mKey;
    }

}
