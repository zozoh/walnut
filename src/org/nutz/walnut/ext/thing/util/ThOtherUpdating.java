package org.nutz.walnut.ext.thing.util;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.castor.Castors;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.lang.util.Regex;
import org.nutz.trans.Proton;
import org.nutz.walnut.api.WnExecutable;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.thing.WnThingService;
import org.nutz.walnut.util.Wn;

public class ThOtherUpdating {

    /**
     * 准备要更新的服务类
     */
    public WnThingService service;

    /**
     * 准备要更新的其他记录
     */
    public List<WnObj> list;

    /**
     * 要修改的元数据
     */
    public NutMap meta;

    /**
     * 要运行的命令列表
     */
    public List<ThingCommandProton> commands;

    private WnExecutable executor;

    public ThOtherUpdating(WnExecutable executor) {
        this.executor = executor;
    }

    public boolean isRunCommands() {
        return null != commands && commands.size() > 0 && null != executor;
    }

    /**
     * @return true 当前的这个任务什么都不做
     */
    public boolean isIdle() {
        if (isRunCommands()) {
            return false;
        }
        if (null == list || list.isEmpty()) {
            return true;
        }
        if (null == meta || meta.isEmpty()) {
            return true;
        }
        if (null == service) {
            return true;
        }
        return false;
    }

    public void doUpdate() {
        // 啥都不需要做
        if (this.isIdle())
            return;

        // 执行命令
        if (this.isRunCommands()) {
            for (Proton<String> pro : commands) {
                String cmdText = pro.invoke();
                if (!Strings.isBlank(cmdText)) {
                    executor.exec(cmdText);
                }
            }
        }
        // 执行更新
        else {
            for (WnObj ot : this.list) {
                this.service.updateThing(ot.id(), this.meta, this.executor, null);
            }
        }
    }

    /**
     * @param meta
     *            要填充的元数据
     * @param sets
     *            填充模板，格式类似
     * 
     *            <pre>
     * {
         "dev_tp" : "@g1",
         "spl_nm" : "@g2",
         "spl_md" : "@g3",
         "spl_nb" : "@g4:int"
       }
     *            </pre>
     * 
     * @param context
     */
    public void fillMeta(NutMap meta, NutMap sets, NutBean context) {
        // 防守一下
        if (null == sets || sets.isEmpty()) {
            return;
        }

        // 开始填充
        Pattern p = Regex.getPattern("^(@[^:]+)(:(int|float|boolean|string))?$");
        for (Map.Entry<String, Object> en : sets.entrySet()) {
            String key = en.getKey();
            Object val = en.getValue();
            // ...............................................
            // 空值
            if (null == val) {
                meta.put(key, null);
            }
            // ...............................................
            // 数字
            else if (val instanceof Number) {
                meta.put(key, val);
            }
            // ...............................................
            // 布尔
            else if (val instanceof Boolean) {
                meta.put(key, val);
            }
            // ...............................................
            // 字符串
            else {
                String str = val.toString();
                Object v2 = null;

                // =xxx
                if (str.startsWith("=")) {
                    v2 = context.get(str.substring(1));
                    meta.put(key, v2);
                    continue;
                }

                // 直接填值
                Matcher m = p.matcher(str);
                if (m.find()) {
                    String k2 = m.group(1);
                    v2 = context.get(k2);
                    // 转换值
                    String valType = m.group(3);
                    if (null != v2 && !Strings.isBlank(valType)) {
                        // int
                        if ("int".equals(valType)) {
                            v2 = Integer.parseInt(v2.toString());
                        }
                        // float
                        else if ("float".equals(valType)) {
                            v2 = Float.parseFloat(v2.toString());
                        }
                        // boolean
                        else if ("boolean".equals(valType)) {
                            v2 = Castors.me().castTo(v2, Boolean.class);
                        }
                        // string
                        else if ("int".equals(valType)) {
                            v2 = v2.toString();
                        }
                        // 靠，不可能
                        else {
                            throw Lang.impossible();
                        }
                    }
                }
                // 否则当做模板
                else {
                    v2 = Tmpl.exec(str, context);
                }

                // 最后展开宏
                if (null != v2 && (v2 instanceof CharSequence))
                    v2 = Wn.fmt_str_macro(v2.toString());

                // 计入
                if (v2 != null) {
                    meta.put(key, v2);
                }
            }
            // ...............................................
        }
    }

}
