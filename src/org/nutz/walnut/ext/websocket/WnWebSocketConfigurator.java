package org.nutz.walnut.ext.websocket;

import javax.websocket.server.ServerEndpointConfig.Configurator;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.Mvcs;

public class WnWebSocketConfigurator extends Configurator {

    public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
        IocBean iocBean = endpointClass.getAnnotation(IocBean.class);
        if (iocBean != null)
            return Mvcs.ctx().getDefaultIoc().get(endpointClass);
        return super.getEndpointInstance(endpointClass);
    }
}
