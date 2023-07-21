package org.nutz.walnut.ext.data.wf.vars;

import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.ext.data.wf.util.WfVarSelectItem;
import org.nutz.walnut.util.Wlang;

public class WfSelectVarLoader extends WfVarLoader {

    private List<WfVarLoader> loaders;

    public WfSelectVarLoader(String varName) {
        super(varName);
        this.loaders = new LinkedList<>();
    }

    @Override
    protected NutBean getBean(NutBean vars) {
        for (WfVarLoader loader : loaders) {
            NutBean bean = loader.getBean(vars);
            if (null == bean || bean.isEmpty()) {
                continue;
            }
            WfVarSelectItem it = Wlang.map2Object(bean, WfVarSelectItem.class);
            if(it.isMatch(vars)) {
                return it.getValue();
            }
        }
        return null;
    }

    public void addLoader(WfVarLoader loader) {
        this.loaders.add(loader);
    }

    public List<WfVarLoader> getLoaders() {
        return loaders;
    }

    public void setLoaders(List<WfVarLoader> loaders) {
        this.loaders = loaders;
    }

}
