package org.nutz.walnut.ext.wup.hdl;

import java.io.IOException;
import java.io.InputStream;

import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

/**
 * 执行一个构建
 * 
 * @author wendal
 *
 */
public class wup_pkg_build implements JvmHdl {

    private static final Log log = Logs.get();
    
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        String app = hc.params.val_check(0);
        if (!app.matches("^([\\w]+)$")) {
            sys.err.println("not allow : " + app);
            return;
        }
        sys.out.println(">> build-" + app);
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"build-" + app});
            p.getOutputStream().close();
            byte[] buf = new byte[128];
            InputStream stdout = p.getInputStream();
            InputStream stderr = p.getErrorStream();
            int len = 0;
            while (p.isAlive()) {
                if (stdout.available() > 0) {
                    len = stdout.read(buf);
                    if (len > 0)
                        sys.out.getOutputStream().write(buf, 0, len);
                }
                if (stderr.available() > 0) {
                    len = stderr.read(buf);
                    if (len > 0)
                        sys.out.getOutputStream().write(buf, 0, len);
                }
            }
        }
        catch (IOException e) {
            sys.err.println("build fail : " + app + " : " + e.getMessage());
            log.info(e.getMessage(), e);
        }
    }

}
