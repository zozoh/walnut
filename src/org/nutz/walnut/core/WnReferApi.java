package org.nutz.walnut.core;

public interface WnReferApi {

    /**
     * 引用加一
     * 
     * @param id
     *            对象ID
     * 
     * @return 修改后的引用计数
     */
    int incOne(String id);

    /**
     * 引用减一
     * 
     * @param id
     *            对象ID
     * 
     * @return 修改后的引用计数
     */
    int decOne(String id);

    /**
     * @param id
     *            对象ID
     * @return 对象当前的引用计数
     */
    int get(String id);

}
