package org.nutz.walnut.core.indexer.localfile;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.Each;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.MimeMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.core.indexer.AbstractIoIndexer;
import org.nutz.walnut.validate.WnValidator;
import org.nutz.walnut.validate.impl.MatchRegex;
import org.nutz.walnut.validate.impl.MatchWildcard;

public class LocalFileIndexer extends AbstractIoIndexer {

    private static final Log log = Logs.get();

    protected File dHome;

    protected String phHome;

    public LocalFileIndexer(WnObj root, MimeMap mimes, File dHome) {
        super(root, mimes);
        this.dHome = dHome;
        this.phHome = Files.getAbsPath(dHome);
    }

    public File getFileHome() {
        return dHome;
    }

    protected File _check_file_by(WnObj p) {
        if (null == p || root.isSameId(p) || root.path().equals(p.path())) {
            return dHome;
        }
        if (p instanceof WnLocalFileObj) {
            WnLocalFileObj lp = (WnLocalFileObj) p;
            File f = lp.getFile();
            // 不在自己的范围内
            String faph = Files.getAbsPath(f);
            if (!faph.startsWith(this.phHome)) {
                throw Er.create("e.io.localfile.OutOfHome", p.path());
            }
            return f;
        }
        throw Er.create("e.io.localfile.unacceptableParent", p.toString());
    }

    protected WnLocalFileObj _gen_file_obj(WnObj p, File f) {
        WnLocalFileObj o = new WnLocalFileObj(root, dHome, f, mimes);
        o.setParent(p);
        return o;
    }

    @Override
    public boolean existsId(String id) {
        File f = Files.getFile(dHome, id);
        return f.exists();
    }

    @Override
    public WnObj checkById(String id) {
        WnObj o = this.get(id);
        if (null == o) {
            throw Er.create("e.io.noexists", id);
        }
        return o;
    }

    @Override
    public WnObj check(WnObj p, String path) {
        WnObj o = this.fetch(p, path);
        if (null == o) {
            throw Er.create("e.io.noexists", path);
        }
        return o;
    }

    @Override
    public WnObj fetch(WnObj p, String path) {
        File f = this._check_file_by(p);
        // 不是目录
        if (!f.isDirectory()) {
            f = f.getParentFile();
        }
        if (path.endsWith("_types.json")) {
            log.infof("LocalFile.fetch : %s :: %s", f.getAbsolutePath(), path);
        }
        // 获取文件
        File f2 = Files.getFile(f, path);
        if (!f2.exists()) {
            return null;
        }
        return _gen_file_obj(p, f2);
    }

    @Override
    public WnObj fetch(WnObj p, String[] paths, int fromIndex, int toIndex) {
        int len = toIndex - fromIndex;
        String path = Strings.join(fromIndex, len, "/", paths);
        return fetch(p, path);
    }

    @Override
    public WnObj fetchByName(WnObj p, String name) {
        return this.fetch(p, name);
    }

    @Override
    public WnObj get(String id) {
        File f = Files.getFile(dHome, id);
        if (!f.exists()) {
            return null;
        }
        return _gen_file_obj(null, f);
    }

    //
    // 遍历
    //

    @Override
    public int eachChild(WnObj o, String name, Each<WnObj> callback) {
        File f = this._check_file_by(o);
        if (f.isFile())
            return 0;

        // 过滤条件
        WnValidator nv = null;
        Object[] args = null;
        if (null != name) {
            args = Lang.array(name);
            if (name.startsWith("!^") || name.startsWith("^")) {
                nv = new MatchRegex();
            } else {
                nv = new MatchWildcard();
            }
        }

        File[] flist = f.listFiles();
        for (int i = 0; i < flist.length; i++) {
            File fi = flist[i];
            // 根据名称过滤
            if (null != nv) {
                String fnm = fi.getName();
                if (!nv.isTrue(fnm, args)) {
                    continue;
                }
            }
            // 生成对象
            WnObj ele = _gen_file_obj(o, fi);
            callback.invoke(i, ele, flist.length);
        }

        return flist.length;
    }

    @Override
    public List<WnObj> getChildren(WnObj o, String name) {
        List<WnObj> list = new LinkedList<>();
        this.eachChild(o, name, new Each<WnObj>() {
            public void invoke(int index, WnObj ele, int length) {
                list.add(ele);
            }
        });
        return list;
    }

    @Override
    public long countChildren(WnObj o) {
        File f = this._check_file_by(o);
        if (f.isFile())
            return 0;
        return f.list().length;
    }

    @Override
    public boolean hasChild(WnObj p) {
        return countChildren(p) > 0;
    }

    //
    // 下面的就是弄个幌子，啥也不做
    //
    @Override
    public void set(WnObj o, String regex) {}

    //
    // 下面的都暂时不实现
    //

    @Override
    public WnObj move(WnObj src, String destPath) {
        throw Lang.noImplement();
    }

    @Override
    public WnObj move(WnObj src, String destPath, int mode) {
        throw Lang.noImplement();
    }

    @Override
    public WnObj rename(WnObj o, String nm) {
        throw Lang.noImplement();
    }

    @Override
    public WnObj rename(WnObj o, String nm, boolean keepType) {
        throw Lang.noImplement();
    }

    @Override
    public WnObj rename(WnObj o, String nm, int mode) {
        throw Lang.noImplement();
    }

    @Override
    public WnObj setBy(String id, NutBean map, boolean returnNew) {
        throw Lang.noImplement();
    }

    @Override
    public WnObj setBy(WnQuery q, NutBean map, boolean returnNew) {
        throw Lang.noImplement();
    }

    @Override
    public int inc(String id, String key, int val, boolean returnNew) {
        throw Lang.noImplement();
    }

    @Override
    public int inc(WnQuery q, String key, int val, boolean returnNew) {
        throw Lang.noImplement();
    }

    @Override
    public int getInt(String id, String key, int dft) {
        throw Lang.noImplement();
    }

    @Override
    public long getLong(String id, String key, long dft) {
        throw Lang.noImplement();
    }

    @Override
    public String getString(String id, String key, String dft) {
        throw Lang.noImplement();
    }

    @Override
    public <T> T getAs(String id, String key, Class<T> classOfT, T dft) {
        throw Lang.noImplement();
    }

    @Override
    public WnObj create(WnObj p, String path, WnRace race) {
        throw Lang.noImplement();
    }

    @Override
    public WnObj create(WnObj p, String[] paths, int fromIndex, int toIndex, WnRace race) {
        throw Lang.noImplement();
    }

    @Override
    public WnObj createById(WnObj p, String id, String name, WnRace race) {
        throw Lang.noImplement();
    }

    @Override
    public WnObj create(WnObj p, WnObj o) {
        throw Lang.noImplement();
    }

    @Override
    public void delete(WnObj o) {
        throw Lang.noImplement();
    }

    @Override
    public WnObj getOne(WnQuery q) {
        throw Lang.noImplement();
    }

    @Override
    public int each(WnQuery q, Each<WnObj> callback) {
        throw Lang.noImplement();
    }

    @Override
    public List<WnObj> query(WnQuery q) {
        throw Lang.noImplement();
    }

    @Override
    public long count(WnQuery q) {
        throw Lang.noImplement();
    }

    @Override
    public WnObj push(String id, String key, Object val, boolean returnNew) {
        throw Lang.noImplement();
    }

    @Override
    public void push(WnQuery query, String key, Object val) {
        throw Lang.noImplement();
    }

    @Override
    public WnObj pull(String id, String key, Object val, boolean returnNew) {
        throw Lang.noImplement();
    }

    @Override
    public void pull(WnQuery query, String key, Object val) {
        throw Lang.noImplement();
    }

}
