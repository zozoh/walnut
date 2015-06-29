package org.nutz.walnut.api.hook;

import java.util.List;

import org.nutz.walnut.api.io.WnObj;

public interface WnHookService {

    List<WnHook> get(String action, WnObj o);

}
