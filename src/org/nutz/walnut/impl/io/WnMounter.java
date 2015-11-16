package org.nutz.walnut.impl.io;

import java.util.List;

import org.nutz.walnut.api.io.MimeMap;
import org.nutz.walnut.api.io.WnObj;

public interface WnMounter {

    /**
     * 根据一个挂载点，获得一个对象
     * 
     * @param mimes
     *            对象的 MIME 类型映射表
     * @param mo
     *            挂载点对象
     * @param paths
     *            路径数组
     * @param fromIndex
     *            从数组哪个下标开始（包含）
     * @param toIndex
     *            到数组哪个下标结束（不包含）
     * 
     * @return 对象
     */
    WnObj get(MimeMap mimes, WnObj mo, String[] paths, int fromIndex, int toIndex);

    /**
     * 根据一个挂载点，获得其子
     * 
     * @param mimes
     *            对象的 MIME 类型映射表
     * @param mo
     *            挂载点对象
     * @param name
     *            子对象名，支持通配符和正则表达式(以^开头)，如果为 null 表示全部子
     * @return
     */
    List<WnObj> getChildren(MimeMap mimes, WnObj mo, String name);

}
