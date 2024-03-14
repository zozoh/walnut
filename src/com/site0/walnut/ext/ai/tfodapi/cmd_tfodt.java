package com.site0.walnut.ext.ai.tfodapi;

import java.util.HashMap;
import java.util.Map;

import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.impl.box.JvmHdlExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;

public class cmd_tfodt extends JvmHdlExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        sys.io.createIfNoExists(null, Wn.normalizeFullPath("~/.tfodt", sys), WnRace.DIR);
        super.exec(sys, args);
    }
    
    public static Map<String, Process> P = new HashMap<>();
}
