package com.site0.walnut.core;

import com.site0.walnut.api.io.WnObj;

public interface WnIoCache {

    /**
     * 根据对象ID命中。每次命中，会重新延长缓冲时间
     * 
     * @param id
     *            对象 ID
     * @return 对象
     */
    WnObj get(String id);

    /**
     * 根据对象路径命中。每次命中，会自动延长缓冲时间。
     * 
     * @param aph
     *            完整路径。（必须 /xx/xxx 形式的，不能是 ~/xxx）
     * @return 对象
     */
    WnObj fetch(String aph);

    /**
     * 根据对象ID移除一个对象，同时也会移除对应路径的映射
     * 
     * @param id
     *            对象 ID
     */
    void remove(String id);

    /**
     * 自动更新一个对象的过期时间
     * 
     * @param obj
     *            对象
     */
    void touch(WnObj obj);

    /**
     * 针对对象进行 ID 和 全路径的索引
     * <p>
     * 如果对象指定了过期时间（expi），那么会自动设置
     * <p>
     * 否则会用实现类自己内部的默认缓冲时间设置过期时间
     * 
     * @param obj
     *            对象
     */
    void save(WnObj obj);

}
