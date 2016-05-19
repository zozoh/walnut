package org.nutz.walnut.ext.job.hdl;

import org.nutz.trans.Atom;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.impl.io.WnEvalLink;
import org.nutz.walnut.job.WnJob;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnContext;

public abstract class job_abstract implements JvmHdl {

    public static WnQuery query(WnSystem sys, String user) {
        WnQuery query = new WnQuery();
        query.setv("pid", jobRootDir(sys).id());
        query.sortBy("job_ava", 1);
        query.setv("job_st", "wait");
        if (user != null)
            query.setv("job_create_user", user);
        return query;
    }
    
    public static void printJob(WnObj jobDir, WnSystem sys, String user) {
        
    }
    
    public static WnObj jobRootDir(WnSystem sys) {
        return sys.io.check(null, WnJob.root);
    }
    
    public static WnUsr rootUser(WnSystem sys) {
        return sys.usrService.check("root");
    }
    
    public static String user(String me, String param) {
        if ("root".equals(me) && param != null)
            return param;
        return me;
    }
    
    public static <T> void sudo(WnSystem sys, Atom atom) {
        WnContext wc = Wn.WC();
        wc.security(new WnEvalLink(sys.io), () -> {
            wc.su(rootUser(sys), atom);
        });
    }
}
