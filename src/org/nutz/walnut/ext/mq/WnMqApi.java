package org.nutz.walnut.ext.mq;

import java.io.Closeable;
import java.util.Set;

/**
 * 封装消息队列相关操作
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public interface WnMqApi extends Closeable {

    Set<String> getTopicSet();

    WnMqHandler getHandler(String topic);

    void subscribe(String topic, WnMqHandler handler) throws WnMqException;

    /**
     * 取消对某一个主题的订阅
     * 
     * @param topic
     *            主题，null 表示取消全部主题
     */
    void unsubscribe(String topic) throws WnMqException;

    void send(String topic, WnMqMessage msg) throws WnMqException;

    void send(String topic, WnMqMessage msg, int timeout) throws WnMqException;

}
