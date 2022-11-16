package org.nutz.walnut.ext.data.unzipx;

import java.nio.charset.Charset;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmFilterContext;

public class UnzipxContext extends JvmFilterContext {

    public WnObj oZip;

    public Charset charset;

}
