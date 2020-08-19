package org.nutz.walnut.ext.mq.impl;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.walnut.ext.mq.WnMqHandler;
import org.nutz.walnut.ext.mq.WnMqMessage;
import org.nutz.walnut.util.WnRun;

@IocBean
public class WnMqDefaultHandler extends WnRun implements WnMqHandler {

    @Override
    public void inovke(WnMqMessage msg) {
        //
    }

}
