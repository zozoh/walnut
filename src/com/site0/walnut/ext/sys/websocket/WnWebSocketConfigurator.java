package com.site0.walnut.ext.sys.websocket;

import javax.websocket.server.ServerEndpointConfig.Configurator;

/**
 * WebSocket 端点配置器。
 * <p>
 * RM-NUTZ-11：去掉对 Nutz Ioc/`Mvcs` 的依赖，直接走容器默认构造。
 * 该端点仅由旧 `web/setup/WnSetup` 注册（随 RM-NUTZ-15 删除）；web2 不注册 WebSocket。
 */
public class WnWebSocketConfigurator extends Configurator {

    public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
        return super.getEndpointInstance(endpointClass);
    }
}
