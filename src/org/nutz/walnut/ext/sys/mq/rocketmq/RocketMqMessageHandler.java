package org.nutz.walnut.ext.sys.mq.rocketmq;

import java.util.List;

import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.nutz.walnut.ext.sys.mq.WnMqApi;
import org.nutz.walnut.ext.sys.mq.WnMqHandler;
import org.nutz.walnut.ext.sys.mq.WnMqMessage;

public class RocketMqMessageHandler implements MessageListenerConcurrently {

    private WnMqApi api;

    public RocketMqMessageHandler(WnMqApi api) {
        this.api = api;
    }

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
                                                    ConsumeConcurrentlyContext context) {
        for (MessageExt msg : msgs) {
            WnMqMessage mqMsg = WnRocketMqApi.fromMessage(msg);
            String topic = msg.getTopic();
            WnMqHandler handler = api.getHandler(topic);
            if (null != handler) {
                handler.inovke(mqMsg);
            }
        }
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }

}
