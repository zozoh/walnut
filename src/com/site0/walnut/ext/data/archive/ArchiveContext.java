package com.site0.walnut.ext.data.archive;

import java.nio.charset.Charset;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.impl.box.JvmFilterContext;

public class ArchiveContext extends JvmFilterContext {

    public WnObj oArchive;
    
    public Charset charset;
    
    public boolean hidden;
    public boolean macosx;
    
    public boolean quiet;
    
    public int count;
    
}
