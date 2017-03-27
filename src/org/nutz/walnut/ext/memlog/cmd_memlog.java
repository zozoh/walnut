package org.nutz.walnut.ext.memlog;

import java.util.List;

import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.MemoryWriterAppender;
import org.nutz.walnut.util.ZParams;

public class cmd_memlog extends JvmExecutor {

    public void exec(WnSystem sys, String[] args) throws Exception {
        if (!sys.usrService.isMemberOfGroup(sys.me, "root")) {
            sys.err.println("permission denied");
            return;
        }
        ZParams params = ZParams.parse(args, null);
        int size = params.vals.length == 0 ? 128 : Integer.parseInt(params.val_check(0));
        List<String> logs = MemoryWriterAppender.cache.getValues();
        if (logs.isEmpty())
            return;
        if (size < logs.size())
            logs.subList(logs.size() - size, logs.size());
        for (String line : logs) {
            if (line.endsWith("\n"))
                sys.out.print(line);
            else
                sys.out.println(line);
        }
    }

}
