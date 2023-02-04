package org.nutz.walnut.ext.data.tmpl;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.impl.box.JvmFilterContext;

public class TmplContext extends JvmFilterContext {

    public NutMap vars;

    public String tmpl;

    public boolean quiet;
}
