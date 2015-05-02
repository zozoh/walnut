package org.nutz.walnut.util;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnSecurity;
import org.nutz.walnut.api.io.WnNode;
import org.nutz.walnut.api.usr.WnSession;

/**
 * 这个是 ThreadLocal 的上下文
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnContext extends NutMap {

    private String seid;

    private WnSession se;

    private String me;

    private WnSecurity security;

    public WnSecurity getSecurity() {
        return security;
    }

    public void setSecurity(WnSecurity callback) {
        this.security = callback;
    }

    public <T extends WnNode> T whenEnter(T nd) {
        if (null != security)
            return security.enter(nd);
        return nd;
    }

    public <T extends WnNode> T whenAccess(T nd) {
        if (null != security)
            return security.access(nd);
        return nd;
    }

    public <T extends WnNode> T whenRead(T nd) {
        if (null != security)
            return security.read(nd);
        return nd;
    }

    public <T extends WnNode> T whenWrite(T nd) {
        if (null != security)
            return security.write(nd);
        return nd;
    }

    public <T extends WnNode> T whenView(T nd) {
        if (null != security)
            return security.view(nd);
        return nd;
    }

    public String checkMe() {
        if (null == me) {
            throw Er.create("e.wc.null.me");
        }
        return me;
    }

    public void me(String me) {
        this.me = me;
    }

    //
    // private String PID;
    //
    // private String frId;
    //
    // private Map<String, Obj> cache;
    //
    // private Callback<Obj> objWatcher;
    //
    // public void dup(WnContext wc) {
    // this.seid = wc.seid;
    // this.me = wc.me;
    // this.PID = wc.PID;
    // this.frId = wc.frId;
    // this.objWatcher = wc.objWatcher;
    // }
    //
    // public void cache(Map<String, Obj> cache) {
    // this.cache = cache;
    // }
    //
    // public Map<String, Obj> cache() {
    // return cache;
    // }
    //
    // public Map<String, Obj> cacheNew() {
    // cache = new HashMap<String, Obj>();
    // return cache;
    // }
    //
    // public void cacheSet(Obj o) {
    // if (null != cache) {
    // cache.put(o.id(), o);
    // }
    // }
    //
    // public Obj cacheGet(String id) {
    // if (null == cache)
    // return null;
    // return cache.get(id);
    // }
    //
    // public Callback<Obj> objWatcher() {
    // return objWatcher;
    // }
    //
    // public void objWatcher(Callback<Obj> oWatcher) {
    // objWatcher = oWatcher;
    // }
    //
    // public String frId() {
    // return frId;
    // }
    //
    // public String checkFrId() {
    // if (null == frId) {
    // throw Er.create("e.wc.null.frId");
    // }
    // return frId;
    // }
    //
    // public void frId(String frId) {
    // this.frId = frId;
    // }
    //
    // public String PID() {
    // return PID;
    // }
    //
    // public String checkPID() {
    // if (null == PID) {
    // throw Er.create("e.wc.null.PID");
    // }
    // return PID;
    // }
    //
    // public void PID(String PID) {
    // this.PID = PID;
    // }
    //
    public String SEID() {
        return seid;
    }

    //
    // public String checkSEID() {
    // if (null == seid) {
    // throw Er.create("e.wc.null.seid");
    // }
    // return seid;
    // }
    //
    public void SEID(String seid) {
        this.seid = seid;
    }

    public WnSession SE() {
        return se;
    }

    public void SE(WnSession se) {
        this.se = se;
        if (null != se) {
            this.seid = se.id();
            this.me = se.me();
        } else {
            this.seid = null;
            this.me = null;
        }
    }

    public WnSession checkSE() {
        if (null == seid) {
            throw Er.create("e.wc.null.seid");
        } else if (null == se) {
            throw Er.create("e.wc.null.se");
        } else if (!se.id().equals(seid)) {
            throw Er.create("e.wc.seid.nomatch");
        }
        return se;
    }

}
