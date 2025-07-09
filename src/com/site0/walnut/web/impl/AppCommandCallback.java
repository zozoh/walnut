package com.site0.walnut.web.impl;

import java.io.IOException;
import java.io.Writer;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Callback;
import org.nutz.trans.Atom;
import com.site0.walnut.api.box.WnBoxContext;
import com.site0.walnut.login.WnSession;
import com.site0.walnut.util.Wn;

/**
 * 命令执行完毕的回调
 * <p>
 * 为了能让客户端每次调用后都能得到最新的会话环境变量的更新，这个回调在命令结果输出后调用。 采用特殊的分隔符，描述一个个的宏命令以便前端执行。
 * <p>
 * 当然，现在仅仅支持下面的两个宏：
 * <ul>
 * <li><code>update_envs</code>: 更新环境变量
 * <li><code>change_session</code>: 切换当前会话
 * </ul>
 * 
 * @see org.nutz.walnut.util.Wn.MACRO#CHANGE_SESSION
 * @see org.nutz.walnut.util.Wn.MACRO#UPDATE_ENVS
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class AppCommandCallback implements Callback<WnBoxContext> {

    private String metaOutputSeparator;
    private Writer w;
    private WnSession session;
    private Atom atom;

    /**
     * @param session
     *            执行命令的会话
     * @param w
     *            文本输出流
     * @param metaOutputSeparator
     *            宏分隔符
     * @param atom
     *            后续操作原子，通常用作释放资源之用，null 表示无需后续操作
     */
    AppCommandCallback(WnSession session, Writer w, String metaOutputSeparator, Atom atom) {
        this.session = session;
        this.w = w;
        this.metaOutputSeparator = metaOutputSeparator;
        this.atom = atom;
    }

    @Override
    public void invoke(WnBoxContext bc) {
        // 有宏的分隔符，表示客户端可以接受更多的宏命令
        if (!Strings.isBlank(metaOutputSeparator)) {
            try {
                // ----------------------------------
                // 无论怎样，都设置环境变量
                w.write("\n" + metaOutputSeparator + ":MACRO:" + Wn.MACRO.UPDATE_ENVS + "\n");
                w.write(Json.toJson(session.getEnv()));
                w.flush();
                // ----------------------------------
                // 修改当前客户端的 session
                if (bc.attrs.has(Wn.MACRO.CHANGE_SESSION)) {
                    String json = Json.toJson(bc.attrs.get(Wn.MACRO.CHANGE_SESSION),
                                              JsonFormat.compact());
                    w.write("\n"
                            + metaOutputSeparator
                            + ":MACRO:"
                            + Wn.MACRO.CHANGE_SESSION
                            + "\n");
                    w.write(json);
                    w.flush();
                }
                // ----------------------------------
            }
            catch (IOException e) {
                throw Wlang.wrapThrow(e);
            }
        }
        // 处理后的回调
        if (atom != null) {
            atom.run();
        }
    }

}