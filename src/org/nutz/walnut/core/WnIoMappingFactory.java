package org.nutz.walnut.core;

import org.nutz.walnut.api.io.WnObj;

public interface WnIoMappingFactory {

    /**
     * 根据顶端映射对象（逐级查找遇到的第一个声明mnt的对象）获取一个映射实例
     * <p>
     * 当然，在我们的假想中，映射不能再包括映射，<br>
     * 这个实现会导致过于复杂，且没有实际应用场景支撑，不值得为此付出代价。
     * 
     * @param homeId
     *            顶端映射对象ID。如果这个接口被调用，它不应该为 null
     * @param mount
     *            映射声明字符串
     * @return 映射对象
     */
    WnIoMapping check(String homeId, String mount);

    /**
     * 根据一个对象，获取其映射。
     * <p>
     * 本函数是一个帮助函数，会自动获取对象的顶端映射对象ID，以及映射信息，<br>
     * 并通过 {@link #check(String, String)} 获取映射对象
     * 
     * @param obj
     *            某个对象
     * @return 映射对象
     */
    WnIoMapping check(WnObj obj);

}
