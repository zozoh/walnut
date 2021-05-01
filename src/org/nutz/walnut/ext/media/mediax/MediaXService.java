package org.nutz.walnut.ext.media.mediax;

import java.net.URI;

/**
 * <code>MediaX</code> 接口的工厂类
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public interface MediaXService {

    /**
     * 创建一个媒体操作接口
     * 
     * @param uri
     *            相关资源，服务类会为其创建对应的 API 实例
     * @param account
     *            平台连接信息的键值。不同的实现类对这个 Key 可以有不同的理解
     * @return 一个媒体接口
     */
    MediaXAPI create(URI uri, String account);

    /**
     * 创建一个媒体操作接口
     * 
     * @param api
     *            指定了接口的 key，相当于直接指定了操作的实现类
     * @param account
     *            平台连接信息的键值。不同的实现类对这个 Key 可以有不同的理解
     * @return 一个媒体接口
     */
    MediaXAPI create(String apiKey, String account);

}
