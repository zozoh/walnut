package com.site0.walnut.impl.box;

import java.io.IOException;
import java.io.OutputStream;

import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.callback.WnStrToken;
import com.site0.walnut.util.callback.WnStrTokenCallback;

public class JvmAtomSubstitutionCallback implements WnStrTokenCallback {

    JvmBoxOutput boxErr;
    private JvmAtomRunner run;

    StringBuilder sb;

    StringBuilder sbOut;
    StringBuilder sbErr;

    OutputStream oldOut;
    OutputStream oldErr;

    JvmAtomSubstitutionCallback(JvmAtomRunner run, JvmBoxOutput boxErr) {
        this.run = run;
        this.boxErr = boxErr;
        this.sb = new StringBuilder();
        this.sbOut = new StringBuilder();
        this.sbErr = new StringBuilder();

        this.oldOut = run.out;
        this.oldErr = run.err;

        run.out = Lang.ops(this.sbOut);
        run.err = Lang.ops(this.sbErr);
    }

    @Override
    public char escape(char c) {
        return '`' == c ? c : 0;
    }

    @Override
    public void invoke(WnStrToken token) {
        switch (token.type) {
        // 预处理引号
        case QUOTE:
            // 执行预处理命令
            String subCmd = token.text.toString();
            run.__run(subCmd, boxErr);
            run.wait_for_idle();

            // 如果出现错误，那么啥也别说了，写到错误输出里
            // 然后返回 null，表示不要往下执行了
            if (null != oldErr && sbErr.length() > 0) {
                try {
                    Streams.write(oldOut, Lang.ins(sbErr));
                }
                catch (IOException e) {
                    throw Er.wrap(e);
                }
            }
            // 成功的话，将输出的内容替换到命令行里
            // 去掉双引号，换行等一切邪恶的东东 >_<
            else {
                String subst = Ws.trim(sbOut.toString().replaceAll("([\r\n\"'])", ""));
                // String subst = sbOut.toString()
                // .replaceAll("([\r\n])", "")
                // .replaceAll("(\"' )", "\\$1");
                sb.append(subst);
            }

            // 清理输出，准备迎接下一个子命令
            sbOut.setLength(0);
            sbErr.setLength(0);
            // 嗯搞定
            break;
        // 普通文字
        case TEXT:
            sb.append(token.text);
            break;
        // 普通引号
        default:
            sb.append(token.quoteC);
            sb.append(token.text);
            sb.append(token.quoteC);
        }
    }
}
