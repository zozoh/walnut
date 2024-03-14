package com.site0.walnut.ext.data.fake;

import java.util.List;

import org.nutz.lang.util.NutBean;
import com.site0.walnut.impl.box.JvmFilterContext;

public class FakeContext extends JvmFilterContext {

    public int number;
    
    public WnFaker<?> faker;

    public FakeOutput output;

    public List<NutBean> beans;

}
