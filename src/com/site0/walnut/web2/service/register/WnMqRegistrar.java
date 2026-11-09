package com.site0.walnut.web2.service.register;

import com.site0.walnut.ext.sys.mq.rocketmq.WnRocketMqApi;
import com.site0.walnut.web.WnConfig;
import com.site0.walnut.web2.service.WnServiceFactory;

/**
 * 装配 {@code messageQueueApi}（旧 {@code conf/ioc/mq.js}）。
 * <p>
 * 仅装配期使用：无状态、无单例，不是运行期服务入口。
 */
public final class WnMqRegistrar {

    private WnMqRegistrar() {}

    public static void register(WnServiceFactory factory) {
        WnConfig conf = factory.getConfig();

        WnRocketMqApi messageQueueApi = new WnRocketMqApi();
        messageQueueApi.setHost(conf.get("mq-host", "localhost"));
        messageQueueApi.setPort(conf.getInt("mq-port", 9876));
        messageQueueApi.setTimeout(conf.getInt("mq-timeout", 5000));
        messageQueueApi.setProducerName("wn-mq-producer");
        messageQueueApi.setConsumerName("wn-mq-consumer");

        factory.register("messageQueueApi", messageQueueApi);
    }

}
