package org.nutz.walnut.ext.ai.tfodapi;

import java.util.HashMap;
import java.util.Map;

import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmHdlExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public class cmd_tfodt extends JvmHdlExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        sys.io.createIfNoExists(null, Wn.normalizeFullPath("~/.tfodt", sys), WnRace.DIR);
        super.exec(sys, args);
    }
    
    public static Map<String, Process> P = new HashMap<>();
}
