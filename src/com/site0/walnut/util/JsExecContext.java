package com.site0.walnut.util;

import java.io.InputStream;
import java.io.OutputStream;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;

public interface JsExecContext {

    String getSessionTicket();

    String getAccountName();

    /**
     * 
     * 便捷根据路径查找对象，如果不存在，返回 null
     * 
     * @param ph
     *            路径，支持环境变量和 ~ 符号
     * @return 对象
     */
    WnObj fetch(String ph);

    /**
     * 便捷根据路径查找对象，如果不存在，抛出哦
     * 
     * @param ph
     *            路径，支持环境变量和 ~ 符号
     * @return 对象
     */
    WnObj check(String ph);

    // 提供低阶 IO 接口
    WnIo io();

    // 这些方法直接委托了 WnSystem
    void exec(String cmdText);

    void execf(String fmt, Object... args);

    void exec(String cmdText, OutputStream stdOut, OutputStream stdErr, InputStream stdIn);

    void exec(String cmdText, StringBuilder stdOut, StringBuilder stdErr, CharSequence stdIn);

    void exec(String cmdText, CharSequence input);

    String exec2(String cmdText);

    String exec2f(String fmt, Object... args);

    String exec2(String cmdText, CharSequence input);

    String json(Object obj);

    Object exec2map(String cmdText);

    Object exec2list(String cmdText);

    String path(String path);

    String readText(String path);

}