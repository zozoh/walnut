package org.nutz.walnut.impl.local.tree;

import java.io.File;

import org.nutz.lang.Lang;
import org.nutz.walnut.api.io.WnHistory;

public class LocalFileWnHistory implements WnHistory {

    private String oid;

    File file;

    public LocalFileWnHistory(String oid, File f) {
        this.oid = oid;
        this.file = f;
    }

    @Override
    public String oid() {
        return oid;
    }

    @Override
    public WnHistory oid(String oid) {
        throw Lang.noImplement();
    }

    @Override
    public long len() {
        return file.length();
    }

    @Override
    public long nanoStamp() {
        return file.lastModified() * 1000000L;
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
    public String sha1() {
        throw Lang.noImplement();
    }

    @Override
    public WnHistory sha1(String sha1) {
        throw Lang.noImplement();
    }

    @Override
    public String data() {
        throw Lang.noImplement();
    }

    @Override
    public WnHistory data(String data) {
        throw Lang.noImplement();
    }

    @Override
    public boolean isSameSha1(String sha1) {
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
