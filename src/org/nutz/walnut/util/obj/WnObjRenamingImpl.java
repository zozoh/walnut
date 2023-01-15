package org.nutz.walnut.util.obj;

import org.nutz.walnut.util.tmpl.WnTmpl;
import org.nutz.lang.util.NutBean;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.util.bean.WnBeanMapping;

public class WnObjRenamingImpl implements WnObjRenaming {

    /**
     * 元数据映射方式
     */
    private WnBeanMapping mapping;

    /**
     * 只保留映射后数据
     */
    private boolean onlyMapping;

    /**
     * 重命名模板
     */
    private WnTmpl nameTmpl;

    @Override
    public String getName(WnObj o) {
        if (null == nameTmpl) {
            return null;
        }
        NutBean vars;
        if (null != mapping) {
            vars = mapping.translate(o, onlyMapping);
        } else {
            vars = o;
        }
        return nameTmpl.render(vars);
    }

    public WnBeanMapping getMapping() {
        return mapping;
    }

    public void setMapping(WnBeanMapping mapping) {
        this.mapping = mapping;
    }

    public boolean isOnlyMapping() {
        return onlyMapping;
    }

    public void setOnlyMapping(boolean onlyMapping) {
        this.onlyMapping = onlyMapping;
    }

    public WnTmpl getNameTmpl() {
        return nameTmpl;
    }

    public void setNameTmpl(WnTmpl nameTmpl) {
        this.nameTmpl = nameTmpl;
    }

    public void setNameTmpl(String str) {
        this.nameTmpl = WnTmpl.parse(str);
    }

}
