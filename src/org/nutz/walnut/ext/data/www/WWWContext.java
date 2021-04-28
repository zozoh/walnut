package org.nutz.walnut.ext.data.www;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.util.ZParams;

public class WWWContext {

    // 当前内容所在目录
    public WnObj oCurrent; 

    public ZParams params;

    public String input;
    
    public String type;
    
    public String fnm;
    
    public NutMap context;

}
