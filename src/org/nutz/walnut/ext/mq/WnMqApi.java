package org.nutz.walnut.ext.mq;

/**
 * 封装消息队列相关操作
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public interface WnMqApi {

    void subscribe(String topic, WnMqMsgHandler handler);
    
    void send(String topic, WnMqMsg msg);

}
