package com.site0.walnut.impl.box.cmd;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import com.site0.walnut.util.Ws;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.ZParams;
import org.nutz.web.ajax.AjaxReturn;

public class cmd_ajaxre extends JvmExecutor {

    private static String _ER = "^(e.[a-zA-Z0-9._-]+)( *: *(.+))?$";
    private static Pattern _E = Pattern.compile(_ER, Pattern.MULTILINE);

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, "cqnse");
        // 准备输出对象
        AjaxReturn re = new AjaxReturn();

        // 得到输入
        String str = params.val(0);
        if (Ws.isBlank(str)) {
            str = sys.in.readAll();
        }
        str = Ws.trim(str);

        // 强制当做错误
        if (params.is("e")) {
            int pos = str.indexOf(':');
            // 拆分一下
            if (pos > 0) {
                re.setErrCode(Ws.trim(str.substring(0, pos)));
                re.setData(Ws.trim(str.substring(pos + 1)));
            }
            // 全当做错误的键
            else {
                re.setErrCode(str);
            }
            // 处理多国语言
            __fill_re_msg(sys, params, re);

            // 嗯
            re.setOk(false);
        }
        // 下面做点判断吧 ...
        else {
            // 看看是不是像错误
            Matcher m = _E.matcher(str);
            if (m.find()) {
                String errCode = m.group(1);
                String reason = Ws.sBlank(m.group(3), null);

                re.setOk(false);
                re.setErrCode(errCode);
                re.setData(reason);

                __fill_re_msg(sys, params, re);
            }
            // 成功:字符串
            else if (params.is("s")) {
                re.setOk(true);
                re.setData(str);
            }
            // 成功
            else {
                re.setOk(true);
                try {
                    re.setData(Json.fromJson(str));
                }
                catch (Throwable e) {
                    re.setData(str);
                }
            }
        }

        // 输出
        JsonFormat jfmt = Cmds.gen_json_format(params);
        sys.out.println(Json.toJson(re, jfmt));
    }

    protected void __fill_re_msg(WnSystem sys, ZParams params, AjaxReturn re) {
        // 得到错误码
        String errCode = params.getString("msg", re.getErrCode());

        if (null == re.getData()) {
            re.setMsg(errCode);
        } else {
            re.setMsg(errCode + " : " + re.getData());
        }
    }

}
