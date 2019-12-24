package org.nutz.walnut.impl.box.cmd;

import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public class cmd_mode extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, null, "^(octal)$");
        String input = params.val_check(0);

        String oct;
        String mds;
        int md;

        // rwxr-x---
        if (input.matches("^[rwx-]{9}$")) {
            mds = input;
            oct = Wn.Io.octalModeFromStr(mds);
            md = Wn.Io.modeFromStr(mds);
        }
        // 777
        else if (params.is("octal")) {
            oct = input;
            md = Wn.Io.modeFromOctalMode(oct);
            mds = Wn.Io.modeToStr(md);
        }
        // 488
        else {
            md = Integer.parseInt(input);
            mds = Wn.Io.modeToStr(md);
            oct = Integer.toOctalString(md);
        }

        // 输出
        sys.out.printlnf("mod: %s", mds);
        sys.out.printlnf("oct: %s", oct);
        sys.out.printlnf("int: %d", md);
    }

}
