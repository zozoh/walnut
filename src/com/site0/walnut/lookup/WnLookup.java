package com.site0.walnut.lookup;

import java.util.List;

import org.nutz.lang.util.NutBean;

public interface WnLookup {

    /**
     * 
     * @param hint
     *            一个字符串表示查询线索，各个实现类可以有自己的不同的理解
     * @param limit
     *            返回的查询记录的数量上限
     * @return 一组查询记录
     */
    List<NutBean> lookup(String hint, int limit);

    /**
     * 根据对象唯一标识获取一条对应记录。
     * <p>
     * 
     * @param id
     *            对象唯一标识
     * @return 查询记录，它返回有可能有下面的结果
     * 
     *         <ul>
     *         <li><code>空列表</code>： 说明没有对应记录
     *         <li><code>包含一个元素的列表</code>： 说明正确的获取了对应记录
     *         <li><code>包含多个元素的列表</code>： 说明你传入的标识不是唯一的
     *         </ul>
     */
    List<NutBean> fetch(String id);

}
