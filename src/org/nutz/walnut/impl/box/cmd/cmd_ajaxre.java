package org.nutz.walnut.impl.box.cmd;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.walnut.ext.i18n.Wni18nService;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.ZParams;
import org.nutz.web.ajax.AjaxReturn;

public class cmd_ajaxre extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, "cqnse");
        // 准备输出对象
        AjaxReturn re = new AjaxReturn();

        // 得到输入
        String str = params.val(0);
        if (Strings.isBlank(str)) {
            str = sys.in.readAll();
        }
        str = Strings.trim(str);

        // 强制当做错误
        if (params.is("e")) {
            int pos = str.indexOf(':');
            // 拆分一下
            if (pos > 0) {
                re.setErrCode(Strings.trim(str.substring(0, pos)));
                re.setData(Strings.trim(str.substring(pos + 1)));
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
            Matcher m = Pattern.compile("^(e.[a-zA-Z0-9.-]+)( *: *(.+))?$").matcher(str);
            if (m.find()) {
                String errCode = m.group(1);
                String reason = Strings.sBlank(m.group(3), null);

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
                re.setData(Json.fromJson(str));
            }
        }

        // 输出
        JsonFormat jfmt = Cmds.gen_json_format(params);
        sys.out.println(Json.toJson(re, jfmt));
    }

    protected void __fill_re_msg(WnSystem sys, ZParams params, AjaxReturn re) {
        // 获取服务类
        Wni18nService i18ns = this.ioc.get(Wni18nService.class);

        // 得到语言
        String lang = params.val(0, sys.getLang());

        // 得到多国语言错误消息
        String errStr = i18ns.getMsg(lang, re.getErrCode());

        if (null == re.getData()) {
            re.setMsg(errStr);
        } else {
            re.setMsg(errStr + " : " + re.getData());
        }
    }

}
