package org.nutz.walnut.ext.mediax;

/**
 * <code>MediaX</code> 接口的工厂类
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public interface MediaXService {

    /**
     * 创建一个媒体操作接口
     * 
     * @param key
     *            平台连接信息的键值。不同的实现类对这个 Key 可以有不同的理解
     * @return 一个媒体接口
     */
    MediaXAPI create(String key);

}
