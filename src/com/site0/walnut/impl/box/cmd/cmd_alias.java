package com.site0.walnut.impl.box.cmd;

import java.util.Map;
import java.util.Map.Entry;

import org.nutz.lang.Each;
import org.nutz.lang.Strings;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;

public class cmd_alias extends JvmExecutor {

    public void exec(final WnSystem sys, String[] args) throws Exception {
        if (args.length == 0)
            return;
        if ("-p".equals(args[0])) {
            sys.jef.each(new Each<Map.Entry<String,JvmExecutor>>() {
                public void invoke(int index, Entry<String, JvmExecutor> ele, int length) {
                    if (ele.getValue() != null && ele.getValue() instanceof cmd_alias_entry) {
                        sys.out.printlnf("%s=%s", ele.getKey(), ((cmd_alias_entry)ele.getValue()).prefix);
                    }
                }
            });
            return;
        }
        if ("-r".equals(args[0]) && args.length > 1) {
            JvmExecutor exec = sys.jef.get(args[1]);
            if (exec != null && exec instanceof cmd_alias_entry) {
                sys.jef.remove(args[0]);
            }
            return;
        }
        for (String alias : args) {
            if (!alias.contains("="))
                continue; // 连等号都没有,跳过
            String[] tmp = alias.split("=", 2);
            if (tmp.length != 2 || Strings.isBlank(tmp[0]) || Strings.isBlank(tmp[1]))
                continue; // 不合法的声明直接跳过
            String key = tmp[0].trim();
            String prefix = tmp[1].trim();
            JvmExecutor exec = sys.jef.get(key);
            if (exec != null && !(exec instanceof cmd_alias_entry)) {
                continue; // 不允许覆盖非alias生成的命令
            }
            sys.jef.put(key, new cmd_alias_entry(key, prefix){});
        }
    }

}

abstract class cmd_alias_entry extends JvmExecutor {

    public String prefix;
    
    public String name;
    
    public cmd_alias_entry(String name, String prefix) {
        this.prefix = prefix;
        this.name = name;
    }

    public void exec(WnSystem sys, String[] args) throws Exception {
        sys.exec(prefix + sys.cmdOriginal.substring(name.length()));
    }
    
}
