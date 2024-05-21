package com.site0.walnut.lookup;

import java.util.List;

import org.nutz.lang.util.NutBean;

public interface WnLookup {

    List<NutBean> lookup(String hint);

}
