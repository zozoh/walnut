package org.nutz.walnut.ext.media.ooml;

import java.util.ArrayList;
import java.util.List;
import org.nutz.lang.util.NutBean;
import org.nutz.walnut.ext.media.ooml.util.OomlRowMapping;
import org.nutz.walnut.impl.box.JvmFilterContext;
import org.nutz.walnut.ooml.OomlEntry;
import org.nutz.walnut.ooml.OomlPackage;
import org.nutz.walnut.ooml.xlsx.XlsxSheet;
import org.nutz.walnut.ooml.xlsx.XlsxWorkbook;

public class OomlContext extends JvmFilterContext {

    public OomlPackage ooml;

    public XlsxWorkbook workbook;

    public XlsxSheet sheet;

    public OomlRowMapping mapping;

    public boolean onlyMapping;

    public OomlEntry currentEntry;

    public List<NutBean> tranlateBeans(List<NutBean> beans) {
        if (null == mapping) {
            return beans;
        }
        List<NutBean> list = new ArrayList<>(beans.size());
        for (NutBean bean : beans) {
            if (null == bean || bean.isEmpty()) {
                continue;
            }
            NutBean b2 = mapping.toBean(bean, onlyMapping);
            if (null != b2 && !b2.isEmpty()) {
                list.add(b2);
            }
        }
        return list;
    }

    public NutBean translateBean(NutBean bean) {
        if (null == mapping) {
            return bean;
        }
        return mapping.toBean(bean, onlyMapping);
    }

}
