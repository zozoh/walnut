package org.nutz.walnut.ext.mq.rocketmq;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.MQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.nutz.lang.Encoding;
import org.nutz.walnut.ext.mq.WnMqApi;
import org.nutz.walnut.ext.mq.WnMqException;
import org.nutz.walnut.ext.mq.WnMqHandler;
import org.nutz.walnut.ext.mq.WnMqMessage;

public class WnRocketMqApi implements WnMqApi {

    private String producerName;

    private String consumerName;

    private String host;

    private int port;

    private int timeout;

    private Map<String, WnMqHandler> handlers;

    private DefaultMQProducer producer;

    private DefaultMQPushConsumer consumer;

    public WnRocketMqApi() {
        handlers = new HashMap<>();
    }

    private MQProducer getProducer() throws WnMqException {
        if (null == producer) {
            synchronized (this) {
                if (null == producer) {
                    String addr = String.format("%s:%d", host, port);
                    producer = new DefaultMQProducer(producerName);
                    producer.setNamesrvAddr(addr);
                    producer.setSendMsgTimeout(timeout);
                    try {
                        producer.start();
                    }
                    catch (MQClientException e) {
                        throw new WnMqException(e);
                    }
                }
            }
        }
        return producer;
    }

    private MQPushConsumer getConsumer() throws WnMqException {
        if (null == consumer) {
            synchronized (this) {
                if (null == consumer) {
                    consumer = new DefaultMQPushConsumer(consumerName);
                    String addr = String.format("%s:%d", host, port);
                    consumer.setNamesrvAddr(addr);
                    consumer.registerMessageListener(new RocketMqMessageHandler(this));
                    try {
                        consumer.start();
                    }
                    catch (MQClientException e) {
                        throw new WnMqException(e);
                    }
                }
            }
        }
        return consumer;
    }

    static Message toMessage(String topic, WnMqMessage mqMsg) {
        Message roMsg = new Message();
        roMsg.setTopic(topic);
        roMsg.setTags(mqMsg.getType().toString());
        roMsg.setBody(mqMsg.toBytes());
        return roMsg;
    }

    static WnMqMessage fromMessage(MessageExt roMsg) {
        WnMqMessage mqMsg = new WnMqMessage();
        String str = new String(roMsg.getBody(), Encoding.CHARSET_UTF8);
        mqMsg.parseText(str);
        return mqMsg;
    }

    @Override
    public synchronized void subscribe(String topic, WnMqHandler handler) throws WnMqException {
        MQPushConsumer cs = this.getConsumer();
        try {
            cs.subscribe(topic, "*");
            handlers.put(topic, handler);
        }
        catch (MQClientException e) {
            throw new WnMqException(e);
        }
    }

    @Override
    public synchronized void unsubscribe(String topic) throws WnMqException {
        MQPushConsumer cs = this.getConsumer();
        cs.unsubscribe(topic);
        handlers.remove(topic);
    }

    @Override
    public synchronized Set<String> getTopicSet() {
        return this.handlers.keySet();
    }

    @Override
    public synchronized WnMqHandler getHandler(String topic) {
        return this.handlers.get(topic);
    }

    @Override
    public void send(String topic, WnMqMessage msg) throws WnMqException {
        try {
            MQProducer pd = this.getProducer();
            Message m = toMessage(topic, msg);
            pd.send(m);
        }
        catch (Exception e) {
            throw new WnMqException(e);
        }
    }

    @Override
    public void send(String topic, WnMqMessage msg, int timeout) throws WnMqException {
        try {
            MQProducer pd = this.getProducer();
            Message m = toMessage(topic, msg);
            pd.send(m, timeout);
        }
        catch (Exception e) {
            throw new WnMqException(e);
        }
    }

    @Override
    public void close() throws IOException {
        // 关闭生产者
        if (null != producer) {
            this.producer.shutdown();
        }
        // 取消订阅者
        try {
            this.unsubscribe(null);
        }
        catch (WnMqException e) {
            throw new IOException(e);
        }
    }

}
