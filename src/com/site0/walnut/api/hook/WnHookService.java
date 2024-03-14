package com.site0.walnut.api.hook;

import java.util.List;

import com.site0.walnut.api.io.WnObj;

public interface WnHookService {

    List<WnHook> get(String action, WnObj o);

}
