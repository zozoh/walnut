package org.nutz.walnut.util;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;

/**
 * 这个是 ThreadLocal 的上下文
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnContext extends NutMap{

//    private String seid;
//
//    private ZSession se;
//
    private String me;
//
//    private String PID;
//
//    private String frId;
//
//    private Map<String, Obj> cache;
//
//    private Callback<Obj> objWatcher;
//
//    public void dup(WnContext wc) {
//        this.seid = wc.seid;
//        this.me = wc.me;
//        this.PID = wc.PID;
//        this.frId = wc.frId;
//        this.objWatcher = wc.objWatcher;
//    }
//
//    public void cache(Map<String, Obj> cache) {
//        this.cache = cache;
//    }
//
//    public Map<String, Obj> cache() {
//        return cache;
//    }
//
//    public Map<String, Obj> cacheNew() {
//        cache = new HashMap<String, Obj>();
//        return cache;
//    }
//
//    public void cacheSet(Obj o) {
//        if (null != cache) {
//            cache.put(o.id(), o);
//        }
//    }
//
//    public Obj cacheGet(String id) {
//        if (null == cache)
//            return null;
//        return cache.get(id);
//    }
//
//    public Callback<Obj> objWatcher() {
//        return objWatcher;
//    }
//
//    public void objWatcher(Callback<Obj> oWatcher) {
//        objWatcher = oWatcher;
//    }
//
//    public String frId() {
//        return frId;
//    }
//
//    public String checkFrId() {
//        if (null == frId) {
//            throw Er.create("e.wc.null.frId");
//        }
//        return frId;
//    }
//
//    public void frId(String frId) {
//        this.frId = frId;
//    }
//
//    public String PID() {
//        return PID;
//    }
//
//    public String checkPID() {
//        if (null == PID) {
//            throw Er.create("e.wc.null.PID");
//        }
//        return PID;
//    }
//
//    public void PID(String PID) {
//        this.PID = PID;
//    }
//
//    public String SEID() {
//        return seid;
//    }
//
//    public String checkSEID() {
//        if (null == seid) {
//            throw Er.create("e.wc.null.seid");
//        }
//        return seid;
//    }
//
//    public void SEID(String seid) {
//        this.seid = seid;
//    }
//
//    public ZSession SE() {
//        return se;
//    }
//
//    public void SE(ZSession se) {
//        this.se = se;
//        if (null != se) {
//            this.seid = se.id();
//            this.me = se.me();
//        } else {
//            this.seid = null;
//            this.me = null;
//        }
//    }
//
//    public ZSession checkSE() {
//        if (null == seid) {
//            throw Er.create("e.wc.null.seid");
//        } else if (null == se) {
//            throw Er.create("e.wc.null.se");
//        } else if (!se.id().equals(seid)) {
//            throw Er.create("e.wc.seid.nomatch");
//        }
//        return se;
//    }
//
//    public String me() {
//        return me;
//    }
//
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
//    public <T> T su(String usr, Proton<T> proton) {
//        String u = me;
//        try {
//            me = usr;
//            proton.run();
//            return proton.get();
//        }
//        finally {
//            me = u;
//        }
//
//    }
//
//    public void su(String usr, Atom atom) {
//        String u = me;
//        try {
//            me = usr;
//            atom.run();
//        }
//        finally {
//            me = u;
//        }
//
//    }
//
//    public void watch(Callback<Obj> objWatcher, Atom atom) {
//        Callback<Obj> ow = this.objWatcher;
//        try {
//            this.objWatcher = objWatcher;
//            atom.run();
//        }
//        finally {
//            this.objWatcher = ow;
//        }
//    }
//
//    public void invokeWatcher(Obj o) {
//        if (null != this.objWatcher)
//            this.objWatcher.invoke(o);
//    }
//
//    public String toString() {
//        return Json.toJson(this,
//                           JsonFormat.compact()
//                                     .setLocked("^se|cache|objWatcher$")
//                                     .setQuoteName(false));
//    }
}
