package org.nutz.walnut.impl.box.cmd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;

/**
 * 这个命令的作用就列出所有命令,除了自己
 * @author wendal(wendal1985@gmail.com)
 *
 */
public class cmd_cmds extends JvmExecutor {

    public void exec(WnSystem sys, String[] args) throws Exception {
        List<String> cmdNames = new ArrayList<>(sys.jef.keys());
        Collections.sort(cmdNames);
        StringBuilder sb = new StringBuilder();
        int lineCount = 10;
        if (args.length > 0) {
            lineCount = Integer.parseInt(args[0]);
        }
        if (lineCount < 1)
            lineCount = 1;
        int count = 0;
        Iterator<String> it = cmdNames.iterator();
        for (int i = 0; i < cmdNames.size(); i++) {
            sb.append(it.next());
            count ++;
            if (count % lineCount == 0) {
                sys.out.println("  " + sb);
                sb.setLength(0);
            } else {
                sb.append(",");
            }
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
            sys.out.println("  " + sb);
        }
    }

}
