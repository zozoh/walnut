package org.nutz.walnut.ext.util.react.action;

import org.nutz.walnut.ext.util.react.bean.ReactAction;

public interface ReactActionHandler {

    void run(ReactActionContext r, ReactAction a);

}
