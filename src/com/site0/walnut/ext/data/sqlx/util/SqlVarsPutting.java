package com.site0.walnut.ext.data.sqlx.util;

import java.util.ArrayList;
import java.util.List;

import org.nutz.lang.util.NutBean;
import org.nutz.mapl.Mapl;

import com.site0.walnut.util.Ws;
import com.site0.walnut.util.tmpl.WnTmplX;

public class SqlVarsPutting {

    /**
     * 
     * @param str
     *            输入的设置表达式，譬如 <code>"code=pet.${id},name=pet.${name}"</code>
     * @return 解析后的对象推入逻辑
     */
    public static SqlVarsPutting[] parse(String str) {
        String[] ss = Ws.splitIgnoreBlank(str);
        List<SqlVarsPutting> re = new ArrayList<>(ss.length);
        for (String s : ss) {
            re.add(new SqlVarsPutting(s));
        }
        return re.toArray(new SqlVarsPutting[re.size()]);
    }

    public static void apply(SqlVarsPutting[] puttings, NutBean bean, NutBean pipeContext) {
        for (SqlVarsPutting putting : puttings) {
            putting.putToBean(bean, pipeContext);
        }
    }

    public static void exec(String puttings, NutBean bean, NutBean pipeContext) {
        SqlVarsPutting[] ings = parse(puttings);
        apply(ings, bean, pipeContext);
    }

    private boolean forceSetBean;

    private boolean forceGetPipe;

    private WnTmplX beanKey;

    private WnTmplX pipeKey;

    /**
     * 
     * @param input
     *            输入的设置表达式，譬如 <code>pet_code=pet.${id}</code>
     */
    public SqlVarsPutting(String input) {
        int pos = input.indexOf('=');
        String from;
        String to;
        if (pos > 0) {
            from = input.substring(0, pos).trim();
            to = input.substring(pos + 1).trim();
        }
        // 默认补全
        else {
            from = input.trim();
            to = input.trim();
        }
        
        // 继续解析
        forceSetBean = false;
        if (from.startsWith("?")) {
            forceSetBean = false;
            from = from.substring(1).trim();
        } else if (from.startsWith("!")) {
            forceSetBean = true;
            from = from.substring(1).trim();
        }

        forceGetPipe = false;
        if (to.startsWith("?")) {
            forceGetPipe = false;
            to = to.substring(1).trim();
        } else if (to.startsWith("!")) {
            forceGetPipe = true;
            to = to.substring(1).trim();
        }

        beanKey = WnTmplX.parse(from);
        pipeKey = WnTmplX.parse(to);
    }

    public void putToBean(NutBean bean, NutBean pipeContext) {
        if (null != beanKey && null != pipeKey) {
            String bean_key = beanKey.render(bean);
            String pipe_key = pipeKey.render(bean);

            Object bean_val = Mapl.cell(bean, bean_key);
            // 除非强制，只有对象没值才设置
            if (!forceSetBean && null != bean_val) {
                return;
            }

            Object pipe_val = Mapl.cell(pipeContext, pipe_key);
            // 除非强制，只有过滤管线上下文有值才设置
            if (!forceGetPipe && null == pipe_val) {
                return;
            }

            // 设置
            Mapl.put(bean, bean_key, pipe_val);
        }
    }

}
