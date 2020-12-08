package org.nutz.walnut.ext.xapi;

/**
 * 第三方接口密钥存储管理器
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public interface ThirdXAkManager {

    /**
     * 实现类应该做好密钥缓存工作
     * 
     * @return 密钥
     */
    String loadAccessKey();

}
