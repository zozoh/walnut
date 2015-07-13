package org.nutz.walnut.ext.fuse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;

public class cmd_fuse extends JvmExecutor {

    private static final Log log = Logs.get();
    
    public void exec(WnSystem sys, String[] args) throws Exception {
        log.debug("hi, fuse args=" + Json.toJson(args));
        if (args.length < 2)
            return;
        List<String> _args = new ArrayList<>(Arrays.asList(args));
        String cmd = _args.remove(0);
        switch (cmd) {
        case "chown":
            
            break;
        case "create":
            
            break;
        case "destroy":
            
            break;
        case "getattr":
            
            break;
        case "mkdir":
            
            break;
        case "read":
            
            break;
        case "readdir":
            
            break;
        case "readlink":
            
            break;
        case "rename":
            
            break;
        case "rmdir":
            
            break;
        case "symlink":
            
            break;
        case "truncate":
            
            break;
        case "unlink":
            
            break;
        case "utimens":
            
            break;
        case "write":
            
            break;
        default:
            break;
        }
    }

}
