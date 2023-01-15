package org.nutz.walnut.ext.data.bizhook;

import java.util.ArrayList;
import java.util.List;

import org.nutz.lang.Strings;
import org.nutz.walnut.util.tmpl.WnTmpl;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.WnExecutable;

/**
 * 封装一个钩子的信息
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class BizHook {

    /**
     * 匹配器列表。'或'的关系，即，如果有一个匹配上了，就算匹配上
     */
    private List<BizHookMatchGroup> match;

    /**
     * 要执行的命令
     */
    private List<WnTmpl> commands;

    public BizHook() {
        match = new ArrayList<>(0);
        commands = new ArrayList<>(0);
    }

    public BizHook(NutMap map) {
        NutMap[] matchs = map.getArray("match", NutMap.class);
        String[] cmds = map.getArray("commands", String.class);
        this.setMatch(matchs);
        this.setCommands(cmds);
    }

    public BizHook(NutMap[] matchs, String[] cmds) {
        this.setMatch(matchs);
        this.setCommands(cmds);
    }

    public void setCommands(String[] cmds) {
        this.commands = new ArrayList<WnTmpl>(cmds.length);
        for (String cmd : cmds) {
            WnTmpl tmpl = WnTmpl.parse(cmd);
            this.commands.add(tmpl);
        }
    }

    public void setMatch(NutMap[] matchs) {
        this.match = new ArrayList<>(matchs.length);
        for (NutMap m : matchs) {
            BizHookMatchGroup mg = new BizHookMatchGroup(m);
            this.match.add(mg);
        }
    }

    public boolean match(NutBean obj) {
        for (BizHookMatchGroup mg : match) {
            if (mg.match(obj)) {
                return true;
            }
        }
        return false;
    }

    public void runCommands(WnExecutable runner, NutBean context) {
        for (WnTmpl tmpl : this.commands) {
            String cmdText = tmpl.render(context);
            runner.exec(cmdText);
        }
    }

    public String toString() {
        int mcount = 0;
        if (null != this.match) {
            mcount = this.match.size();
        }
        String cmds = Strings.join(";", this.commands);
        return String.format("M(%d): %s", mcount, cmds);
    }
}
