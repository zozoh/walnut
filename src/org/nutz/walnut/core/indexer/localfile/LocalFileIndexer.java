package org.nutz.walnut.core.indexer.localfile;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.ContinueLoop;
import org.nutz.lang.Each;
import org.nutz.lang.ExitLoop;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Disks;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.MimeMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.api.io.agg.WnAggOptions;
import org.nutz.walnut.api.io.agg.WnAggResult;
import org.nutz.walnut.core.indexer.AbstractIoIndexer;
import org.nutz.walnut.util.Wlang;
import org.nutz.walnut.util.WnSort;
import org.nutz.walnut.util.validate.WnMatch;
import org.nutz.walnut.util.validate.impl.AutoMatch;
import org.nutz.walnut.util.validate.impl.AutoStrMatch;

public class LocalFileIndexer extends AbstractIoIndexer {

    // private static final Log log = Wlog.getIO();

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

    @Override
    public WnAggResult aggregate(WnQuery q, WnAggOptions agg) {
        throw Wlang.noImplement();
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
        // 获取基线目录对象（可能是 P 也可能是 HOME）
        File f = this._check_file_by(p);
        // 不是目录
        if (!f.isDirectory()) {
            f = f.getParentFile();
            // 退到根了
            if (f.equals(this.dHome)) {
                p = this.root;
            }
            // 否则重新搞一个对象
            else {
                p = new WnLocalFileObj(root, dHome, f, mimes);
            }
        }
        // 相对于基线文件，调整 path，去掉内部的 ..
        File f2 = Files.getFile(f, path);
        if (!f2.exists()) {
            return null;
        }
        String fph = Disks.getCanonicalPath(f2.getAbsolutePath());
        // 这个路径超出了索引管理器管理的路径，可能会造成危险
        if (!fph.startsWith(this.phHome)) {
            throw Er.create("e.io.localFile.OutOfHome", path);
        }
        f2 = new File(fph);
        // return _gen_file_obj(p, f2);

        // 如果输入的 path 带上了 ../ 这种回退的路径，那么输入的 p 就不是返回对象真正的父了
        // 就留着一个空吧
        if (path.indexOf("../") >= 0) {
            return _gen_file_obj(null, f2);
        }
        return _gen_file_obj(p, f2);

        //
        // 为了保险起见，重新生成一遍父对象
        // zozoh@20201118: 我也忘记了为啥要这么搞，好像是某个 case
        // 以后遇到了要标注一下，NND，真是萝卜快了不洗泥啊！
        // zozoh@20201119: 想起来了，是因为 ../ 这种路径导致需要重新获取 P
        // 下面的逻辑有点复杂，其实只要把 p 设置为 null 应该就可以了
        // 观察一段时间，如果 OK，下面的逻辑就可以删掉了
        // WnObj p2;
        // File fP = f2.getParentFile();
        // if (fP.equals(this.dHome)) {
        // p2 = this.root;
        // } else {
        // p2 = new WnLocalFileObj(this.root, dHome, fP, mimes);
        // // 如果原来的父有个 parent, 为了保持原来的路径，还是要设置一下
        // if ((p instanceof WnLocalFileObj)) {
        // WnLocalFileObj lp = (WnLocalFileObj) p;
        // if (null != lp._parent) {
        // p2.setParent(lp._parent);
        // }
        // }
        // }
        //
        // // 放心大胆的生成新文件对象吧
        // return _gen_file_obj(p2, f2);
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
        WnMatch nv = null;
        if (null != name) {
            nv = new AutoStrMatch(name);
        }

        File[] flist = f.listFiles();
        for (int i = 0; i < flist.length; i++) {
            File fi = flist[i];
            // 根据名称过滤
            if (null != nv) {
                String fnm = fi.getName();
                if (!nv.match(fnm)) {
                    continue;
                }
            }
            // 生成对象
            WnObj ele = _gen_file_obj(o, fi);
            // 回调
            try {
                callback.invoke(i, ele, flist.length);
            }
            catch (ExitLoop e) {
                break;
            }
            catch (ContinueLoop e) {}
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

    @Override
    public WnObj getOne(WnQuery q) {
        q.limit(1);
        List<WnObj> list = query(q);
        if (list.isEmpty())
            return null;
        return list.get(0);
    }

    @Override
    public int each(WnQuery q, Each<WnObj> callback) {

        // 准备过滤条件
        // 只支持 nm 和 tp 和 lm 和 len
        NutMap flt = q.first().pick("nm", "tp", "lm", "len");
        WnMatch ma = flt.isEmpty() ? null : new AutoMatch(flt);

        // 准备父目录
        String pid = q.first().getString("pid");
        File dir = null;
        // 默认用主目录
        if (Strings.isBlank(pid) || this.root.isSameId(pid)) {
            dir = this.dHome;
        }
        // 否则选择目录
        // 进入到这个方法， IO 层已经把两段式 pid 使用了，仅会传第二段 ID 过来
        else {

            dir = Files.getFile(dHome, pid);
            if (!dir.exists()) {
                throw Er.create("e.io.localfile.NoExists", pid);
            }
            if (!dir.isDirectory()) {
                throw Er.create("e.io.localfile.MustBeDirectory", pid);
            }
        }

        // 依次查询
        File[] files = dir.listFiles();
        int limit = q.limit();
        int skip = q.skip();
        int max = files.length - skip;
        int count = 0;
        int reLen = limit > 0 ? Math.min(limit, max) : max;
        ArrayList<NutMap> sortedList = new ArrayList<>(reLen);

        // 指定了父对象，需要设置到归档里
        WnObj oP = q.getParentObj();

        // 进入匹配循环
        for (int i = 0; i < max; i++) {
            File f = files[i + skip];

            // 准备对象
            NutMap meta = new NutMap();
            if (f.isDirectory()) {
                meta.put("race", WnRace.DIR.name());
            } else if (f.isFile()) {
                meta.put("race", WnRace.FILE.name());
            }
            meta.put("nm", f.getName());
            String ftp = Files.getSuffixName(f);
            if (null != ftp) {
                ftp = ftp.toLowerCase();
                meta.put("tp", ftp);
                String mime = mimes.getMime(ftp, null);
                if (null != mime) {
                    meta.put("mime", mime);
                }
            }
            meta.put("lm", f.lastModified());
            meta.put("len", f.length());
            meta.put("_file", f);

            // 匹配
            if (null != ma && !ma.match(meta))
                continue;

            // 计入预排序列表
            sortedList.add(meta);

            // 计数
            count++;
            if (limit > 0 && count >= limit)
                break;
        }

        // 没有回调，直接就返回了
        if (null == callback)
            return count;

        // 需要排序
        WnSort[] sorts = WnSort.makeList(q.sort());
        if (null != sorts && sorts.length > 0) {
            sortedList.sort(new Comparator<NutMap>() {
                public int compare(NutMap o1, NutMap o2) {
                    return WnSort.compare(sorts, o1, o2);
                }
            });
        }

        // 调用回调
        for (int i = 0; i < sortedList.size(); i++) {
            NutMap meta = sortedList.get(i);
            File f = meta.getAs("_file", File.class);
            WnLocalFileObj o = new WnLocalFileObj(root, dHome, f, mimes);
            o.setParent(oP);
            callback.invoke(i, o, max);
        }

        return count;
    }

    @Override
    public List<WnObj> query(WnQuery q) {
        List<WnObj> list = new LinkedList<>();
        this.each(q, new Each<WnObj>() {
            public void invoke(int index, WnObj o, int length) {
                list.add(o);
            }
        });
        return list;
    }

    @Override
    public long count(WnQuery q) {
        return this.each(q, new Each<WnObj>() {
            public void invoke(int index, WnObj ele, int length) {}
        });
    }

    //
    // 下面的就是弄个幌子，啥也不做
    //
    @Override
    public void set(WnObj o, String regex) {}

    @Override
    public WnObj setBy(String id, NutBean map, boolean returnNew) {
        return this.get(id);
    }

    @Override
    public WnObj setBy(WnQuery q, NutBean map, boolean returnNew) {
        throw Lang.noImplement();
    }

    @Override
    public int inc(String id, String key, int val, boolean returnNew) {
        return val;
    }

    @Override
    public int inc(WnQuery q, String key, int val, boolean returnNew) {
        return val;
    }

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
    public int getInt(String id, String key, int dft) {
        return dft;
    }

    @Override
    public long getLong(String id, String key, long dft) {
        return dft;
    }

    @Override
    public String getString(String id, String key, String dft) {
        return dft;
    }

    @Override
    public <T> T getAs(String id, String key, Class<T> classOfT, T dft) {
        return dft;
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
