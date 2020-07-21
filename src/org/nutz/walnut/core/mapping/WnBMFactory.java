package org.nutz.walnut.core.mapping;

import org.nutz.walnut.core.WnIoBM;

public interface WnBMFactory {

    /**
     * 根据顶端映射对象（逐级查找遇到的第一个声明mnt的对象）获取一个桶管理器实例。
     * <p>
     * 当然，根索引管理器直接管理的对象，不会有顶端映射对象，实现者必须要考虑到这一点。
     * 
     * @param homeId
     *            顶端映射对象Id。可能为空
     * @param str
     *            一个关于桶管理器实现的配置字符串，除了不能包括<code>()</code>，其他随意，<br>
     *            由具体实现类自行理解起意义。<br>
     *            通常为一个桶管理器实现类需要的配置线索。<br>
     *            可能是一个预定义的键，甚至也可能是一个路径，通过这个路径，<br>
     *            实现类在能获得 WnIo接口的前提下，可以读取这个配置文件，<br>
     *            从而让桶管理器的创建具备更大的扩展性和可能性。
     * @return 桶管理器的实现类
     */
    WnIoBM load(String homeId, String str);

}
