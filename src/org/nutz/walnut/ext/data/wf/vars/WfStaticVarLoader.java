package org.nutz.walnut.ext.data.wf.vars;

import java.util.List;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.ext.data.wf.util.WfVarSelectItem;

public class WfStaticVarLoader extends WfVarLoader {

    private NutBean bean;

    private List<WfVarSelectItem> selectItems;

    public WfStaticVarLoader(String varName) {
        super(varName);
    }

    public WfStaticVarLoader(String varName, NutBean bean) {
        super(varName);
        this.bean = bean;
    }

    @Override
    protected NutBean getBean(NutBean vars) {
        return bean;
    }

    public void setBean(NutBean bean) {
        this.bean = bean;
    }

    public List<WfVarSelectItem> getSelectItems() {
        return selectItems;
    }

    public void setSelectItems(List<WfVarSelectItem> selectItems) {
        this.selectItems = selectItems;
    }

}
