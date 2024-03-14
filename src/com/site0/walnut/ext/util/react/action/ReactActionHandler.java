package com.site0.walnut.ext.util.react.action;

import com.site0.walnut.ext.util.react.bean.ReactAction;

public interface ReactActionHandler {

    void run(ReactActionContext r, ReactAction a);

}
