package org.nutz.walnut.ext.data.tmpl;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.impl.box.JvmFilterContext;
import org.nutz.walnut.util.tmpl.WnTmplTokenExpert;

public class TmplContext extends JvmFilterContext {

    public NutMap vars;

    public String tmpl;

    public WnTmplTokenExpert expert;

    public boolean quiet;

    public boolean showKeys;
}
