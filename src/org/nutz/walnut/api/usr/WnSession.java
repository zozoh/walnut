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
     * @param se
     *            另外一个会话对象
     * @return 两个会话对象是否相等
     */
    boolean isSame(WnSession se);

    /**
     * @param seid
     *            另外一个会话对象ID
     * @return 两个会话对象是否相等
     */
    boolean isSame(String seid);

    /**
     * @param se
     *            另外一个会话对象
     * @return 给定会话是否是自己的子会话
     */
    boolean isParentOf(WnSession se);

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
     * 将用户对象的信息加载到会话环境变量中
     * 
     * @param u
     *            用户对象
     * @return 环境变量集合
     */
    NutMap putUsrVars(WnUsr u);

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
     * @param nm
     *            变量名
     * @return 是否存在某变量
     */
    boolean hasVar(String nm);

    /**
     * @param nm
     *            变量名
     * @return 某变量的值的字符串形式
     */
    String varString(String nm);
    
    String varString(String nm, String dft);

    /**
     * @param nm
     *            变量名
     * @return 某变量的值的整数形式，默认值为最小整数
     */
    int varInt(String nm);
    
    /**
     * @param nm
     *            变量名
     * @param dft
     *            默认值
     * @return 某变量的值的整数形式
     */
    int varInt(String nm, int dft);

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
     * @return 会话的截止时间（绝对毫秒数）
     */
    long expireTime();

    /**
     * @return 一个当前会话的新实例
     */
    WnSession clone();

    /**
     * @return 显示成可以让客户端看到的 Map 对象
     */
    NutMap toMapForClient();

    /**
     * @param jfmt
     *            JSON 的格式化配置
     * @return 会话的 JSON 描述
     */
    String toJson(JsonFormat jfmt);
}