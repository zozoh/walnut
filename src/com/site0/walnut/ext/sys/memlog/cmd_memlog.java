package com.site0.walnut.ext.sys.memlog;

import java.util.List;

import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.login.role.WnRoleList;
import com.site0.walnut.login.usr.WnUser;
import com.site0.walnut.util.MemoryWriterAppender;
import com.site0.walnut.util.ZParams;

public class cmd_memlog extends JvmExecutor {

    public void exec(WnSystem sys, String[] args) throws Exception {
        WnUser me = sys.getMe();
        WnRoleList roles = sys.auth.getRoles(me);
        if (!roles.isMemberOfRole("root")) {
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
