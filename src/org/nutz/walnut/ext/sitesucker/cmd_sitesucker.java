package org.nutz.walnut.ext.sitesucker;

import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

/**
 * 爬网站
 * 
 * 
 * @author pw
 *
 */
public class cmd_sitesucker extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, null);

    }

}