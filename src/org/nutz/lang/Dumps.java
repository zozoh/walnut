package org.nutz.lang;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.regex.Matcher;

/**
 * 显示对象的信息，为日志以及调试提供帮助的函数集
 * 
 * @author zozoh(zozohtnt@gmail.com)
 * @author wendal(wendal1985@gmail.com)
 * @author bonyfish(mc02cxj@gmail.com)
 */
public abstract class Dumps {

    /**
     * 显示 Matcher 的详细信息
     * 
     * @param m
     *            Matcher 对象，必须未执行过 find
     * @return 信息
     */
    public static String matcher(Matcher m) {
        if (m.find())
            return matcherFound(m);
        return "No found!";
    }

    /**
     * 显示 Matcher 的详细信息
     * 
     * @param m
     *            Matcher 对象，必须执行过 find
     * @return 信息
     */
    public static String matcherFound(Matcher m) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%d/%d  Regin:%d/%d\n",
                                m.start(),
                                m.end(),
                                m.regionStart(),
                                m.regionEnd()));
        for (int i = 0; i <= m.groupCount(); i++)
            sb.append(String.format("%2d:[%3d,%3d) `%s`\n", i, m.start(i), m.end(i), m.group(i)));
        return sb.toString();
    }

    /**
     * 显示一个对象所有个 getter 函数返回，以及 public 的 Field 的值
     * 
     * @param obj
     *            对象
     * @return 信息
     */
    public static String obj(Object obj) {
        if (null == obj)
            return "null";
        StringBuilder sb = new StringBuilder(obj.getClass().getName() + "\n\n[Fields:]");
        Mirror<?> mirror = Mirror.me(obj.getClass());
        for (Field f : mirror.getType().getFields())
            if (Modifier.isPublic(f.getModifiers()))
                try {
                    sb.append(String.format("\n\t%10s : %s", f.getName(), f.get(obj)));
                }
                catch (Exception e1) {
                    sb.append(String.format("\n\t%10s : %s", f.getName(), e1.getMessage()));
                }
        sb.append("\n\n[Methods:]");
        for (Method m : mirror.getType().getMethods())
            if (Modifier.isPublic(m.getModifiers()))
                if (m.getName().startsWith("get"))
                    if (m.getParameterTypes().length == 0)
                        try {
                            sb.append(String.format("\n\t%10s : %s", m.getName(), m.invoke(obj)));
                        }
                        catch (Exception e) {
                            sb.append(String.format("\n\t%10s : %s", m.getName(), e.getMessage()));
                        }
        return sb.toString();
    }

    /**
     * 显示 HTTP 内容的名称空间
     */


}
