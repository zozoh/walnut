package org.nutz.walnut.api.usr;

import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutMap;

/**
 * 会话接口
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public interface WnSession {

    /**
     * @return 会话ID
     */
    String id();

    /**
     * @return 是否有父会话
     */
    boolean hasParentSession();

    /**
     * @return 父会话 ID
     */
    String getParentSessionId();

    /**
     * 得到环境变量集合。推荐懒加载的方式
     * 
     * @return 所有的环境遍历集合
     */
    NutMap vars();

    /**
     * 设置某一个环境变量（并不持久化）即，重新读取会话的话，会看不见这个更改，除非执行 save
     * 
     * @param nm
     *            变量名
     * @param val
     *            变量值，如果 null 表示删除
     * @return 会话自身以便链式赋值
     */
    WnSession var(String nm, Object val);

    /**
     * @param nm
     *            变量名
     * @return 某变量的值
     */
    Object var(String nm);

    /**
     * 将自身所有变量持久化到会话对象中，其他进程可以随时读取
     */
    void save();

    /**
     * @return 当前登录的账户名
     */
    String me();

    /**
     * @return 当前登录的组名
     */
    String group();

    /**
     * @return 会话持续时间（毫秒）
     */
    long duration();

    /**
     * @return 一个当前会话的新实例
     */
    WnSession clone();

    /**
     * @param fmt
     *            JSON 格式化信息
     * @return 显示成可以让客户端看到的 JSON 对象
     */
    NutMap toMapForClient(JsonFormat fmt);
}