package org.nutz.walnut.impl.local.data;

import org.nutz.lang.Lang;
import org.nutz.walnut.api.io.WnHistory;
import org.nutz.walnut.api.io.WnObj;

public class LocalObjWnHistory implements WnHistory {

    private WnObj o;

    public LocalObjWnHistory(WnObj o) {
        this.o = o;
    }

    @Override
    public String oid() {
        return o.id();
    }

    @Override
    public long len() {
        return o.len();
    }

    @Override
    public long nanoStamp() {
        return o.nanoStamp();
    }

    @Override
    public String sha1() {
        return o.sha1();
    }

    @Override
    public boolean isSameSha1(String sha1) {
        return o.isSameSha1(sha1);
    }

    @Override
    public String data() {
        return o.data();
    }

    @Override
    public WnHistory oid(String oid) {
        throw Lang.noImplement();
    }

    @Override
    public WnHistory len(long len) {
        throw Lang.noImplement();
    }

    @Override
    public WnHistory nanoStamp(long nano) {
        throw Lang.noImplement();
    }

    @Override
    public WnHistory sha1(String sha1) {
        throw Lang.noImplement();
    }

    @Override
    public WnHistory data(String data) {
        throw Lang.noImplement();
    }

    @Override
    public String owner() {
        throw Lang.noImplement();
    }

    @Override
    public WnHistory owner(String ow) {
        throw Lang.noImplement();
    }

}
