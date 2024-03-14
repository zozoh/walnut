package com.site0.walnut.core.mapping;

import com.site0.walnut.api.io.WnIoIndexer;
import com.site0.walnut.api.io.WnObj;

public interface WnIndexerFactory {

    /**
     * 根据顶端映射对象（逐级查找遇到的第一个声明mnt的对象）获取一个索引管理器实例。
     * 
     * @param oHome
     *            顶端映射对象。如果这个接口被调用，它不应该为 null
     * @param str
     *            一个关于索引管理器实现的配置字符串，除了不能包括<code>()</code>，其他随意，<br>
     *            由具体实现类自行理解起意义。<br>
     *            通常为一个索引管理器实现类需要的配置线索。<br>
     *            可能是一个预定义的键，甚至也可能是一个路径，通过这个路径，<br>
     *            实现类在能获得 WnIo接口的前提下，可以读取这个配置文件，<br>
     *            从而让索引管理器的创建具备更大的扩展性和可能性。
     * @return 索引管理器器的实现类
     */
    WnIoIndexer load(WnObj oHome, String str);

}
