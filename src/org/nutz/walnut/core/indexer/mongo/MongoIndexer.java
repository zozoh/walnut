package org.nutz.walnut.core.indexer.mongo;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.nutz.lang.ContinueLoop;
import org.nutz.lang.Each;
import org.nutz.lang.ExitLoop;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Callback;
import org.nutz.lang.util.NutMap;
import org.nutz.mongo.ZMoCo;
import org.nutz.mongo.ZMoDoc;
import org.nutz.trans.Atom;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.MimeMap;
import org.nutz.walnut.api.io.WalkMode;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.api.io.WnSecurity;
import org.nutz.walnut.core.WnIoMapping;
import org.nutz.walnut.core.WnIoMappingFactory;
import org.nutz.walnut.core.bean.WnIoObj;
import org.nutz.walnut.core.indexer.AbstractIoIndexer;
import org.nutz.walnut.impl.io.WnBean;
import org.nutz.walnut.impl.io.mongo.WnMongos;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnContext;

import com.mongodb.Bytes;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class MongoIndexer extends AbstractIoIndexer {

    private WnIoMappingFactory mappings;

    private ZMoCo co;

    protected MongoIndexer(WnObj root, MimeMap mimes, ZMoCo co, WnIoMappingFactory mappings) {
        super(root, mimes);
        this.co = co;
        this.mappings = mappings;
    }

    private WnObj __enter_dir(WnObj o, WnSecurity secu) {
        WnObj dir = secu.enter(o, false);
        // 肯定遇到了链接目录
        if (!dir.isSameId(o)) {
            dir.name(o.name());
            dir.setParent(o.parent());
        }
        return dir;
    }

    private WnObj _fetch_one_by_name(WnObj p, String name) {
        ZMoDoc q = ZMoDoc.NEW("pid", p.id()).putv("nm", name);
        ZMoDoc doc = co.findOne(q);
        WnIoObj obj = Mongos.toWnObj(doc);
        obj.setIndexer(this);
        return obj;
    }

    /**
     * 修改一个对象所有祖先的同步时间。当然，未设置同步的祖先会被无视
     * 
     * @param tree
     *            元数据读写接口
     * @param o
     *            对象
     * @param includeSelf
     *            是否也检视自身的同步时间
     */
    private void update_ancestor_synctime(final WnObj o, final boolean includeSelf) {
        WnContext wc = Wn.WC();

        // 防止无穷递归
        if (wc.isSynctimeOff())
            return;

        final List<WnObj> list = new LinkedList<WnObj>();
        o.loadParents(list, false);
        final long synctime = System.currentTimeMillis();
        wc.synctimeOff(new Atom() {
            @Override
            public void run() {
                for (WnObj an : list) {
                    if (an.syncTime() > 0) {
                        an.syncTime(synctime);
                        _set_quiet(an, "^st$");
                    }
                }
                if (includeSelf && o.syncTime() > 0) {
                    o.syncTime(synctime);
                    _set_quiet(o, "^st$");
                }
            }
        });
    }

    private void _set_quiet(WnObj o, String regex) {
        NutMap map = o.toMap4Update(regex);
        String id = o.id();
        _set(id, map);
    }

    private void _set(String id, NutMap map) {
        if (map.size() > 0) {
            ZMoDoc q = Mongos.qID(id);
            ZMoDoc doc = __map_to_doc_for_update(map);

            // 执行更新
            co.update(q, doc, true, false);
        }
    }

    private ZMoDoc __map_to_doc_for_update(NutMap map) {
        ZMoDoc doc = ZMoDoc.NEW();

        // 提炼字段
        for (Map.Entry<String, Object> en : map.entrySet()) {
            String key = en.getKey();
            Object val = en.getValue();
            boolean unset = key.startsWith("!");
            if (unset)
                key = key.substring(1);

            // ID 字段不能被修改
            if ("id".equals(key)) {
                continue;
            }
            // 如果为空，则表示 unset
            if (unset) {
                doc.unset(key);
            }
            // 其他的字段
            else {
                doc.set(key, val);
            }
        }
        return doc;
    }

    private WnIoObj _set_by(WnQuery q, NutMap map, boolean returnNew) {
        WnIoObj o = null;

        // 必须得有条件
        if (null == q || q.isEmptyMatch()) {
            return null;
        }

        // 更新或者创建
        if (map.size() > 0) {
            ZMoDoc qDoc = Mongos.toQueryDoc(q);
            ZMoDoc update = __map_to_doc_for_update(map);
            ZMoDoc sort = ZMoDoc.NEW(q.sort());

            // 执行更新
            ZMoDoc doc = co.findAndModify(qDoc, null, sort, false, update, returnNew, false);

            // 执行结果
            if (null != doc) {
                o = Mongos.toWnObj(doc);
                o.setIndexer(this);
            }
        }

        // 返回
        return o;
    }

    @Override
    public WnObj fetch(WnObj p, String path) {
        if (path.startsWith("/")) {
            p = null;
        }
        String[] ss = Strings.splitIgnoreBlank(path, "[/]");
        return fetch(p, ss, 0, ss.length);
    }

    @Override
    public WnObj fetch(WnObj p, String[] paths, int fromIndex, int toIndex) {
        // null 表示从根路径开始
        if (null == p) {
            p = root.clone();
        }
        // ................................................
        // 尝试从后查找，如果有 id:xxx 那么就截断，因为前面的就木有意义了
        for (int i = toIndex - 1; i >= fromIndex; i--) {
            String nm = paths[i];
            if (nm.startsWith("id:")) {
                p = this.get(nm.substring(3));
                if (null == p)
                    return null;
                fromIndex = i + 1;
                break;
            }
        }
        // ................................................
        // 用尽路径元素了，则直接返回
        if (fromIndex >= toIndex)
            return p;
        // ................................................
        // 确保读取所有的父
        p.loadParents(null, false);
        // ................................................
        // 得到节点检查的回调接口
        WnContext wc = Wn.WC();
        WnSecurity secu = wc.getSecurity();

        if (null != secu) {
            p = __enter_dir(p, secu);
        }

        // 确保是目录
        if (!p.isDIR()) {
            p = p.parent();
        }
        // ................................................
        // 处理挂载节点
        if (p.isMount()) {
            WnIoMapping mapping = mappings.check(p);
            return mapping.fetch(p, paths, fromIndex, toIndex);
        }
        // ................................................
        // 逐个进入目标节点的父
        WnObj nd;
        String nm;
        int lastIndex = toIndex - 1;
        for (int i = fromIndex; i < lastIndex; i++) {
            // 因为支持回退上一级，所以有可能 p 为空
            if (null == p) {
                p = root.clone();
            }

            nm = paths[i];

            // 就是当前
            if (".".equals(nm)) {
                continue;
            }

            // 回退一级
            if ("..".equals(nm)) {
                nd = p.parent();
                p = nd;
                continue;
            }
            // 子节点采用的通配符或者正则表达式
            // - 通配符 "*" 会在 WnQuery 转成真正查询条件时，正则表达式化
            if (nm.startsWith("^") || nm.contains("*")) {
                WnQuery q = Wn.Q.pid(p).setv("nm", nm).limit(1);
                nd = Lang.first(this.query(q));
            }
            // 找子节点，找不到，就返回 null
            else {
                nd = this._fetch_one_by_name(p, nm);
            }

            // 找不到了，就返回
            if (null == nd)
                return null;

            // 设置节点
            nd.setParent(p);
            nd.path(p.path()).appendPath(nd.name());

            // 确保节点可进入
            if (null != secu) {
                nd = __enter_dir(nd, secu);
            }

            // 处理挂载节点
            if (nd.isMount()) {
                WnIoMapping mapping = mappings.check(nd);
                return mapping.fetch(nd, paths, i + 1, toIndex);
            }

            // 指向下一个节点
            p = nd;
        }
        // ................................................
        // 最后再检查一下目标节点
        nm = paths[lastIndex];

        // 就是返回自己
        if (nm.equals(".")) {
            return p;
        }

        // 纯粹返回上一级
        if (nm.equals("..")) {
            return p.parent();
        }

        // 因为支持回退上一级，所以有可能 p 为空
        if (null == p) {
            p = root.clone();
        }

        // 目标是通配符或正则表达式
        if (nm.startsWith("^") || nm.contains("*")) {
            WnQuery q = Wn.Q.pid(p).setv("nm", nm).limit(1);
            nd = Lang.first(this.query(q));
        }
        // 仅仅是普通名称
        else {
            nd = this._fetch_one_by_name(p, nm);
        }
        // ................................................
        // 最后，可惜，还是为空
        if (null == nd)
            return null;
        // ................................................
        // 设置节点
        nd.setParent(p);
        nd.path(p.path()).appendPath(nd.name());
        // ................................................
        // 确保节点可以访问
        nd = wc.whenAccess(nd, true);

        // ................................................
        // 搞定了，返回吧
        return nd;
    }

    @Override
    public WnObj move(WnObj src, String destPath, int mode) {
        // 目标是空的，啥也不做
        if (Strings.isBlank(destPath))
            return src;

        // 不用移动
        String srcPath = this.checkById(src.id()).path();
        if (srcPath.equals(destPath)) {
            return src;
        }

        // 将自己移动到自己的子里面，这个不能够啊
        if (destPath.startsWith(srcPath) && srcPath.lastIndexOf('/') < destPath.lastIndexOf('/')) {
            throw Er.create("e.io.mv.parentToChild");
        }

        // 肯定要移动了 ...
        WnContext wc = Wn.WC();

        // 调用钩子
        wc.doHook("move", src);

        // 保存之前的 d0,d1
        String old_d0 = src.d0();
        String old_d1 = src.d1();

        // 得到自身的原始的父
        WnObj oldSrcParent = src.parent();

        // 确保源是可移除的
        src = wc.whenRemove(src, false);

        // 看看目标是否存在
        String newName = null;
        String taPath = destPath;
        WnObj ta = fetch(null, taPath);

        // 准备最后更新的正则表达式
        String regex = "d0|d1|pid";

        // 如果不存在，看看目标的父是否存在，并且可能也同时要改名
        if (null == ta) {
            taPath = Files.getParent(taPath);
            ta = fetch(null, taPath);
            newName = Files.getName(destPath);
        }
        // 如果存在的是一个文件
        else if (ta.isFILE()) {
            throw Er.create("e.io.obj.exists", destPath);
        }

        // 还不存在不能忍啊
        if (null == ta) {
            throw Er.create("e.io.obj.noexists", taPath);
        }

        // 确认目标能写入
        ta = wc.whenWrite(ta, false);

        // 改变名称和类型
        if (null != newName) {
            src.name(newName);
            regex += "|nm";

            // 还要同时更新类型，好吧
            if (Wn.MV.isTP(mode)) {
                Wn.set_type(mimes, src, null);
                regex += "|tp|mime";
            }
        }

        // 改变父
        src.setParent(ta);

        // 更新一下索引的记录
        _set_quiet(src, "^(" + regex + ")$");

        // 如果是目录，且d0,d1 改变了，需要递归
        if (src.isDIR()) {
            final String d0 = src.d0();
            final String d1 = src.d1();
            if (!Lang.equals(d0, old_d0) || !Lang.equals(d1, old_d1)) {
                this.walk(src, new Callback<WnObj>() {
                    public void invoke(WnObj obj) {
                        obj.d0(d0).d1(d1);
                        _set_quiet(obj, "^d0|d1$");
                    }
                }, WalkMode.DEPTH_NODE_FIRST);
            }
        }

        // 触发同步
        if (Wn.MV.isSYNC(mode)) {
            // 触发同步时间修改
            update_ancestor_synctime(src, false);

            // 如果对象换了父节点，之前的父节点也要被触发修改时间
            if (!oldSrcParent.isSameId(src.parentId())) {
                update_ancestor_synctime(oldSrcParent, true);
            }
        }

        // 返回
        return src;
    }

    @Override
    public void set(WnObj o, String regex) {
        NutMap map = o.toMap4Update(regex);

        // 改名
        String nm = map.getString("nm");
        map.remove("nm");

        // 改动路径
        String pid = map.getString("pid");
        map.remove("pid");

        // 移动到另外的目录
        if (!Strings.isBlank(pid)) {
            String newPath = this.checkById(pid).path();
            if (!Strings.isBlank(nm)) {
                newPath = Wn.appendPath(newPath, nm);
            } else {
                newPath = Wn.appendPath(newPath, o.name());
            }
            this.move(o, newPath);
        }
        // 仅仅是改名
        else if (!Strings.isBlank(nm)) {
            this.rename(o, nm);
        }

        // 确保对象有写权限
        o = Wn.WC().whenMeta(o, false);

        // 修改元数据
        _set(o.id(), map);
    }

    @Override
    public WnObj setBy(WnQuery q, NutMap map, boolean returnNew) {
        // 不支持改名和移动目录
        if (null != map) {
            map.remove("nm");
            map.remove("pid");
        }
        // 确保对象存在，并有写权限
        WnObj o = this.getOne(q);
        if (null == o)
            return null;

        o = Wn.WC().whenMeta(o, false);

        // 执行修改
        if (map.size() > 0) {
            // 这里再次确保只匹配一个
            q.limit(1);

            // 执行设置
            WnObj o1 = _set_by(q, map, returnNew);

            if (null != o1) {
                o1.remove("ph");
            }
            o = o1;
        }

        // 返回修改前内容
        return o;
    }

    @Override
    public int inc(WnQuery q, String key, int val, boolean returnNew) {
        ZMoDoc qDoc = Mongos.toQueryDoc(q);
        ZMoDoc fields = ZMoDoc.NEW(key, 1);
        ZMoDoc update = ZMoDoc.NEW().m("$inc", key, val);
        ZMoDoc sort = ZMoDoc.NEW(q.sort());

        ZMoDoc doc = co.findAndModify(qDoc, fields, sort, false, update, returnNew, false);

        return doc.getInt(key);
    }

    @Override
    public int getInt(String id, String key, int dft) {
        ZMoDoc q = ZMoDoc.NEW("id", id);
        ZMoDoc flds = ZMoDoc.NEW(key, 1);
        ZMoDoc doc = co.findOne(q, flds);
        return doc.getInt(key, dft);
    }

    @Override
    public long getLong(String id, String key, long dft) {
        ZMoDoc q = ZMoDoc.NEW("id", id);
        ZMoDoc flds = ZMoDoc.NEW(key, 1);
        ZMoDoc doc = co.findOne(q, flds);
        return doc.getLong(key, dft);
    }

    @Override
    public String getString(String id, String key, String dft) {
        ZMoDoc q = ZMoDoc.NEW("id", id);
        ZMoDoc flds = ZMoDoc.NEW(key, 1);
        ZMoDoc doc = co.findOne(q, flds);
        return doc.getString(key, dft);
    }

    @Override
    public <T> T getAs(String id, String key, Class<T> classOfT, T dft) {
        ZMoDoc q = ZMoDoc.NEW("id", id);
        ZMoDoc flds = ZMoDoc.NEW(key, 1);
        ZMoDoc doc = co.findOne(q, flds);
        return doc.getAs(key, classOfT, dft);
    }

    @Override
    public WnObj create(WnObj p, String path, WnRace race) {
        return null;
    }

    @Override
    public WnObj create(WnObj p, String[] paths, int fromIndex, int toIndex, WnRace race) {
        return null;
    }

    @Override
    public WnObj createById(WnObj p, String id, String name, WnRace race) {
        return null;
    }

    @Override
    public void delete(WnObj o) {}

    @Override
    public WnObj get(String id) {
        return null;
    }

    @Override
    public int each(WnQuery q, Each<WnObj> callback) {
        // 木有必要迭代
        if (null == callback)
            return 0;

        // 准备查询
        ZMoDoc qDoc = null == q ? ZMoDoc.NEW() : Mongos.toQueryDoc(q);
        DBCursor cu = co.find(qDoc);
        final boolean autoPath = Wn.WC().isAutoPath();
        final WnContext wc = Wn.WC();

        try {
            cu.addOption(Bytes.QUERYOPTION_NOTIMEOUT);
            int i = 0;
            int n = 0;
            int count = cu.count();
            Mongos.setup_paging(cu, q);
            Mongos.setup_sorting(cu, q);

            int limit = null == q ? 0 : q.limit();

            while (cu.hasNext()) {
                // 如果设置了分页 ...
                if (limit > 0 && n >= limit) {
                    break;
                }
                // 获取对象
                DBObject dbobj = cu.next();
                WnIoObj o = Mongos.toWnObj(dbobj);
                o.setIndexer(this);

                // 检查访问权限
                o = (WnIoObj) wc.whenAccess(o, true);
                if (null == o)
                    continue;

                // 确保有全路径
                if (autoPath)
                    o.path();

                // 回调
                try {
                    callback.invoke(i++, o, count);
                    n++;
                }
                catch (ExitLoop e) {
                    break;
                }
                catch (ContinueLoop e) {}
            }

            return n;
        }
        finally {
            cu.close();
        }
    }

    @Override
    public List<WnObj> getChildren(WnObj o, String name) {
        if (o == null)
            o = root.clone();

        // 确保解开了链接
        o = Wn.WC().whenEnter(o, false);

        // 挂载点
        if (o.isMount()) {
            WnIoMapping mapping = mappings.check(o);
            return mapping.getChildren(o, name);
        }

        // 否则，直接查询子
        WnQuery q = Wn.Q.pid(o);
        if (null != name)
            q.setv("nm", name);
        q.asc("nm");
        return query(q);
    }

    @Override
    public long count(WnQuery q) {
        if (q == null)
            throw new RuntimeException("count without WnQuery is not allow");
        ZMoDoc qDoc = Mongos.toQueryDoc(q);
        if (qDoc.isEmpty())
            throw new RuntimeException("count with emtry WnQuery is not allow");

        // 对id的正则表达式进行更多的检查
        if (qDoc.containsField("id")) {
            Object tmp = qDoc.get("id");
            if (tmp != null && tmp instanceof Pattern && tmp.toString().equals("^")) {
                throw new RuntimeException("count with id:/^/ is not allow");
            }
        }

        return co.count(qDoc);
    }

    @Override
    public boolean hasChild(WnObj p) {
        return co.count(ZMoDoc.NEW("pid", p.id())) != 0;
    }

    @Override
    public WnObj push(String id, String key, Object val, boolean returnNew) {
        return null;
    }

    @Override
    public void push(WnQuery query, String key, Object val) {}

    @Override
    public WnObj pull(String id, String key, Object val, boolean returnNew) {
        return null;
    }

    @Override
    public void pull(WnQuery query, String key, Object val) {}

}
