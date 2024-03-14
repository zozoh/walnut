package com.site0.walnut.ext.data.tmpl;

import org.nutz.lang.util.NutMap;
import com.site0.walnut.impl.box.JvmFilterContext;
import com.site0.walnut.util.tmpl.WnTmplTokenExpert;

public class TmplContext extends JvmFilterContext {

    public NutMap vars;

    public String tmpl;

    public WnTmplTokenExpert expert;

    public boolean quiet;

    public boolean showKeys;
}
