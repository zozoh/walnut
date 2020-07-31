package org.nutz.walnut.core.io;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.nutz.img.Images;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Each;
import org.nutz.lang.Encoding;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Callback;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.MimeMap;
import org.nutz.walnut.api.io.WalkMode;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.api.io.WnSecurity;
import org.nutz.walnut.core.WnIoActionCallback;
import org.nutz.walnut.core.WnIoHandle;
import org.nutz.walnut.core.WnIoHandleMutexException;
import org.nutz.walnut.core.WnIoIndexer;
import org.nutz.walnut.core.WnIoMapping;
import org.nutz.walnut.core.WnIoMappingFactory;
import org.nutz.walnut.core.bean.WnObjMapping;
import org.nutz.walnut.core.stream.WnIoInputStream;
import org.nutz.walnut.core.stream.WnIoOutputStream;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnContext;

public class WnIoImpl2 implements WnIo {

    private static final Log log = Logs.get();

    /**
     * 映射工厂类
     */
    private WnIoMappingFactory mappings;

    /**
     * 子类可以添加一个删除动作的回调。
     * <p>
     * 因为删除动作可能涉及递归深层删除，因此不能用在子类函数覆盖super方法进行拦截
     */
    protected WnIoActionCallback whenDelete;

    /**
     * 子类可以设置一个写操作的回调。
     * <p>
     * 在打开输出流时，会附加这个回调，当流关闭时，也会调用。
     * <p>
     * !!! 注意，直接采用句柄操作，则不会触发这个回调
     */
    protected WnIoActionCallback whenWrite;

    public WnIoImpl2(WnIoMappingFactory mappings) {
        this.mappings = mappings;
    }

    // 考虑到 copyData 操作除了涉及 BM 也涉及到 indexer，所以主要操作逻辑放到 mapping 层比较合适
    // 如果不在同样的映射桶内，则，只能通过流 copy 了
    @Override
    public long copyData(WnObj a, WnObj b) {
        WnIoMapping ma = mappings.checkMapping(a);
        WnIoMapping mb = mappings.checkMapping(b);
        // 调试日志
        if (log.isDebugEnabled()) {
            log.debugf("copyData ma:%s, mb:%s",
                       ma.getClass().getSimpleName(),
                       mb.getClass().getSimpleName());
        }
        // 相同桶管理器可以快速 Copy
        if (ma.isSameBM(mb)) {
            if (log.isDebugEnabled()) {
                log.debug("Quick copy");
            }
            return ma.copyData(a, b);
        }

        // 否则只能硬 COPY 了
        long re = 0;
        WnIoHandle h_a = null;
        WnIoHandle h_b = null;
        try {
            h_a = ma.open(a, Wn.S.R);
            h_b = mb.open(b, Wn.S.W);
            if (log.isDebugEnabled()) {
                log.debugf("copyData open h_a:%s, h_b:%s", h_a, h_b);
            }
            byte[] buf = new byte[8192];
            int len;
            while ((len = h_a.read(buf, 0, buf.length)) > 0) {
                re += len;
                h_b.write(buf, 0, len);
            }
        }
        catch (IOException e) {
            throw Lang.wrapThrow(e);
        }
        catch (WnIoHandleMutexException e) {
            throw Lang.wrapThrow(e);
        }
        // 确保关闭
        finally {
            // 关闭读
            try {
                h_a.close();
            }
            catch (Exception e) {
                log.warn("Fail to close A:" + h_a, e);
            }
            // 刷新写缓冲
            try {
                h_b.flush();
            }
            catch (Exception e) {
                log.warn("Fail to flush B:" + h_b, e);
            }
            // 关闭写缓冲
            try {
                h_b.close();
            }
            catch (Exception e) {
                log.warn("Fail to close B:" + h_b, e);
            }
        }
        if (log.isDebugEnabled()) {
            log.debugf("copyData done: %d", re);
        }
        return re;
    }

    @Override
    public WnIoHandle openHandle(WnObj o, int mode) throws WnIoHandleMutexException {
        WnIoMapping im = mappings.checkMapping(o);
        return im.open(o, mode);
    }

    @Override
    public String open(WnObj o, int mode) {
        throw Lang.noImplement();
    }

    @Override
    public WnObj flush(String hid) {
        throw Lang.noImplement();
    }

    @Override
    public WnObj close(String hid) {
        throw Lang.noImplement();
    }

    @Override
    public int read(String hid, byte[] bs, int off, int len) {
        throw Lang.noImplement();
    }

    @Override
    public void write(String hid, byte[] bs, int off, int len) {
        throw Lang.noImplement();
    }

    @Override
    public int read(String hid, byte[] bs) {
        throw Lang.noImplement();
    }

    @Override
    public void write(String hid, byte[] bs) {
        throw Lang.noImplement();
    }

    @Override
    public void seek(String hid, long pos) {
        throw Lang.noImplement();
    }

    @Override
    public long getPos(String hid) {
        throw Lang.noImplement();
    }

    // 考虑到 copyData 操作除了涉及 BM 也涉及到 indexer，所以主要操作逻辑放到 mapping 层比较合适
    @Override
    public void delete(WnObj o) {
        this.delete(o, false);
    }

    @Override
    public void delete(WnObj o, boolean r) {
        WnIoMapping im = mappings.checkMapping(o);
        im.delete(o, r, this.whenDelete);

        // 更新同步时间
        WnIoIndexer indexer = im.getIndexer();
        Wn.Io.update_ancestor_synctime(indexer, o, false, 0);
    }

    @Override
    public void trancate(WnObj o, long len) {
        WnIoMapping im = mappings.checkMapping(o);
        im.truncate(o, len);
    }

    @Override
    public boolean exists(WnObj p, String path) {
        // null 表示从根路径开始
        if (null == p || path.startsWith("/")) {
            p = mappings.getRoot();
        }
        WnIoMapping im = mappings.checkMapping(p);
        WnObj obj = im.getIndexer().fetch(p, path);
        return null != obj;
    }

    @Override
    public boolean existsId(String id) {
        WnObjMapping om = mappings.checkById(id);
        WnIoIndexer indexer = om.getSubIndexer();

        // 查询
        return indexer.existsId(om.getMyId());
    }

    @Override
    public WnObj checkById(String id) {
        // 直接来吧
        WnObj o = this.get(id);
        if (null == o) {
            throw Er.create("e.io.obj.noexists", "id:" + id);
        }
        return o;
    }

    @Override
    public WnObj check(WnObj p, String path) {
        WnObj o = fetch(p, path);
        if (null == o)
            throw Er.create("e.io.obj.noexists", path);
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
        WnIoIndexer globalIndexer = mappings.getGlobalIndexer();
        // null 表示从根路径开始
        if (null == p) {
            p = mappings.getRoot();
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

        // 检查权限或者展开链接目录
        if (null != secu) {
            p = secu.enter(p, false);
        }

        // 确保是目录
        if (!p.isDIR()) {
            p = p.parent();
        }
        // ................................................
        // 处理挂载节点
        if (p.isMount()) {
            WnIoMapping mapping = mappings.checkMapping(p);
            return mapping.getIndexer().fetch(p, paths, fromIndex, toIndex);
        }
        // ................................................
        // 逐个进入目标节点的父
        WnObj nd;
        String nm;
        int lastIndex = toIndex - 1;
        for (int i = fromIndex; i < lastIndex; i++) {
            // 因为支持回退上一级，所以有可能 p 为空
            if (null == p) {
                p = mappings.getRoot();
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
                nd = globalIndexer.fetchByName(p, nm);
            }

            // 找不到了，就返回
            if (null == nd)
                return null;

            // 设置节点
            nd.setParent(p);
            nd.path(p.path()).appendPath(nd.name());

            // 确保节点可进入
            if (null != secu) {
                nd = secu.enter(nd, false);
            }

            // 处理挂载节点
            if (nd.isMount()) {
                WnIoMapping mapping = mappings.checkMapping(nd);
                return mapping.getIndexer().fetch(nd, paths, i + 1, toIndex);
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
            p = mappings.getRoot();
            ;
        }

        // 目标是通配符或正则表达式
        if (nm.startsWith("^") || nm.contains("*")) {
            WnQuery q = Wn.Q.pid(p).setv("nm", nm).limit(1);
            nd = Lang.first(this.query(q));
        }
        // 仅仅是普通名称
        else {
            nd = globalIndexer.fetchByName(p, nm);
        }
        // ................................................
        // 最后，可惜，还是为空
        if (null == nd)
            return null;
        // ................................................
        // 设置节点
        nd.setParent(p);
        // ................................................
        // 确保节点可以访问
        nd = wc.whenAccess(nd, true);

        // ................................................
        // 搞定了，返回吧
        return nd;
    }

    @Override
    public void walk(WnObj p, Callback<WnObj> callback, WalkMode mode) {
        WnIoMapping mapping = mappings.checkMapping(p);
        WnIoIndexer indexer = mapping.getIndexer();
        indexer.walk(p, callback, mode);
    }

    @Override
    public WnObj move(WnObj src, String destPath) {
        // TODO 这里还没考虑到在不同的映射间怎么移动的问题
        WnObjMapping om = mappings.checkById(src.id());
        WnIoIndexer indexer = om.getSelfIndexer();
        return indexer.move(src, destPath);
    }

    @Override
    public WnObj move(WnObj src, String destPath, int mode) {
        // TODO 这里还没考虑到在不同的映射间怎么移动的问题
        WnObjMapping om = mappings.checkById(src.id());
        WnIoIndexer indexer = om.getSelfIndexer();
        return indexer.move(src, destPath, mode);
    }

    @Override
    public WnObj rename(WnObj o, String nm) {
        WnObjMapping om = mappings.checkById(o.id());
        WnIoIndexer indexer = om.getSelfIndexer();
        return indexer.rename(o, nm);
    }

    @Override
    public WnObj rename(WnObj o, String nm, boolean keepType) {
        WnObjMapping om = mappings.checkById(o.id());
        WnIoIndexer indexer = om.getSelfIndexer();
        return indexer.rename(o, nm, keepType);
    }

    @Override
    public WnObj rename(WnObj o, String nm, int mode) {
        WnObjMapping om = mappings.checkById(o.id());
        WnIoIndexer indexer = om.getSelfIndexer();
        return indexer.rename(o, nm, mode);
    }

    @Override
    public void set(WnObj o, String regex) {
        WnObjMapping om = mappings.checkById(o.id());
        WnIoIndexer indexer = om.getSelfIndexer();
        Wn.Io.eval_dn(o);
        indexer.set(o, regex);
    }

    @Override
    public WnObj setBy(String id, NutBean map, boolean returnNew) {
        __save_map_for_update_meta(map);
        WnObjMapping om = mappings.checkById(id);
        WnIoIndexer indexer = om.getSelfIndexer();
        return indexer.setBy(om.getMyId(), map, returnNew);
    }

    @Override
    public WnObj setBy(WnQuery q, NutBean map, boolean returnNew) {
        __save_map_for_update_meta(map);
        // 声明了 ID 转到 setBy(id)
        String id = q.first().getString("id");
        if (!Strings.isBlank(id)) {
            return this.setBy(id, map, returnNew);
        }

        // 如果声明了 pid ，则看看有木有映射
        String pid = q.first().getString("pid");
        if (!Strings.isBlank(pid)) {
            WnObjMapping om = mappings.checkById(pid);
            WnIoIndexer indexer = om.getSubIndexer();
            // 确保 pid 是子ID
            q.setv("pid", om.getMyId());
            return indexer.setBy(q, map, returnNew);
        }
        // 采用根索引管理器
        WnIoIndexer indexer = mappings.getGlobalIndexer();
        return indexer.setBy(q, map, returnNew);
    }

    @Override
    public WnObj setBy(String id, String key, Object val, boolean returnNew) {
        NutMap map = Lang.map(key, val);
        return this.setBy(id, map, returnNew);
    }

    @Override
    public WnObj setBy(WnQuery q, String key, Object val, boolean returnNew) {
        return this.setBy(q, Lang.map(key, val), returnNew);
    }

    @Override
    public int inc(String id, String key, int val, boolean returnNew) {
        WnObjMapping om = mappings.checkById(id);
        WnIoIndexer indexer = om.getSelfIndexer();
        return indexer.inc(om.getMyId(), key, val, returnNew);
    }

    @Override
    public int inc(WnQuery q, String key, int val, boolean returnNew) {
        // 声明了 ID 转到 setBy(id)
        String id = q.first().getString("id");
        if (!Strings.isBlank(id)) {
            WnObjMapping om = mappings.checkById(id);
            WnIoIndexer indexer = om.getSelfIndexer();
            return indexer.inc(om.getMyId(), key, val, returnNew);
        }

        // 如果声明了 pid ，则看看有木有映射
        String pid = q.first().getString("pid");
        if (!Strings.isBlank(pid)) {
            WnObjMapping om = mappings.checkById(pid);
            WnIoIndexer indexer = om.getSubIndexer();
            // 确保 pid 是子ID
            q.setv("pid", om.getMyId());
            return indexer.inc(q, key, val, returnNew);
        }
        // 采用根索引管理器
        WnIoIndexer indexer = mappings.getGlobalIndexer();
        return indexer.inc(q, key, val, returnNew);
    }

    @Override
    public int getInt(String id, String key, int dft) {
        WnObjMapping om = mappings.checkById(id);
        WnIoIndexer indexer = om.getSelfIndexer();
        return indexer.getInt(om.getMyId(), key, dft);
    }

    @Override
    public long getLong(String id, String key, long dft) {
        WnObjMapping om = mappings.checkById(id);
        WnIoIndexer indexer = om.getSelfIndexer();
        return indexer.getLong(om.getMyId(), key, dft);
    }

    @Override
    public String getString(String id, String key, String dft) {
        WnObjMapping om = mappings.checkById(id);
        WnIoIndexer indexer = om.getSelfIndexer();
        return indexer.getString(om.getMyId(), key, dft);
    }

    @Override
    public <T> T getAs(String id, String key, Class<T> classOfT, T dft) {
        WnObjMapping om = mappings.checkById(id);
        WnIoIndexer indexer = om.getSelfIndexer();
        return indexer.getAs(om.getMyId(), key, classOfT, dft);
    }

    @Override
    public WnObj create(WnObj p, String path, WnRace race) {
        // 是否从树的根部创建
        if (null == p || path.startsWith("/")) {
            p = this.getRoot();
        }

        // 分析路径
        String[] ss = Strings.splitIgnoreBlank(path, "[/]");
        String[] paths = new String[ss.length];
        int len = 0;
        for (String s : ss) {
            // 回退
            if ("..".equals(s)) {
                len = Math.max(len - 1, 0);
            }
            // 当前
            else if (".".equals(s)) {
                continue;
            }
            // 增加
            else {
                paths[len++] = s;
            }
        }

        // 创建
        return create(p, paths, 0, len, race);
    }

    @Override
    public WnObj create(WnObj p, String[] paths, int fromIndex, int toIndex, WnRace race) {
        // 默认从自己的根开始
        if (null == p) {
            p = this.getRoot();
        }

        // 准备创建
        final int rightIndex = toIndex - 1;
        final WnObj p0 = p;
        final WnContext wc = Wn.WC();
        final WnIoIndexer globalIndexer = mappings.getGlobalIndexer();

        // 检查所有的父是否都被创建
        WnObj p1 = p0;
        for (int i = fromIndex; i < rightIndex; i++) {
            String name = paths[i];
            WnObj nd = globalIndexer.fetchByName(p1, name);
            // 确保节点可以进入
            nd = wc.whenEnter(nd, false);

            // 有节点的话继续下一个路径
            if (null != nd) {
                // 已经是映射了
                if (nd.isMount()) {
                    WnIoMapping mapping = mappings.checkMapping(nd);
                    WnIoIndexer indexer = mapping.getIndexer();
                    return indexer.create(nd, paths, i + 1, toIndex, race);
                }
                // 继续下一层路径
                p1 = nd;
                continue;
            }
            // 没有节点，创建目录节点Ï
            for (; i < rightIndex; i++) {
                p1 = globalIndexer.createById(p1, null, paths[i], WnRace.DIR);
            }
        }

        // 创建自身节点
        return createById(p1, null, paths[rightIndex], race);
    }

    @Override
    public WnObj createById(WnObj p, String id, String name, WnRace race) {
        // null 表示从根路径开始
        if (null == p) {
            p = mappings.getRoot();
        }
        WnIoMapping mapping = mappings.checkMapping(p);
        WnIoIndexer indexer = mapping.getIndexer();
        WnObj o = indexer.createById(p, id, name, race);

        // 更新父节点同步时间
        Wn.Io.update_ancestor_synctime(indexer, o, false, 0);

        return o;
    }

    @Override
    public WnObj createIfNoExists(WnObj p, String path, WnRace race) {
        // null 表示从根路径开始
        if (null == p || path.startsWith("/")) {
            p = mappings.getRoot();
        }
        WnIoMapping mapping = mappings.checkMapping(p);
        WnIoIndexer indexer = mapping.getIndexer();
        WnObj o = indexer.fetch(p, path);

        // 存在就返回
        if (null != o) {
            // 种类冲突，不能忍啊
            if (!o.isRace(race))
                throw Er.create("e.io.create.invalid.race", path + " ! " + race);
            return o;
        }
        // 不存在，就创建
        o = indexer.create(p, path, race);

        // 更新父节点同步时间
        Wn.Io.update_ancestor_synctime(indexer, o, false, 0);

        return o;
    }

    @Override
    public WnObj createIfExists(WnObj p, String path, WnRace race) {
        // null 表示从根路径开始
        if (null == p || path.startsWith("/")) {
            p = mappings.getRoot();
        }
        WnIoMapping mapping = mappings.checkMapping(p);
        WnIoIndexer indexer = mapping.getIndexer();
        WnObj o = indexer.fetch(p, path);
        // 如果存在，删了以便创建心的
        if (null != o) {
            mapping.delete(o, true, this.whenDelete);
        }
        // 先删除再创建
        o = indexer.create(p, path, race);

        // 更新父节点同步时间
        Wn.Io.update_ancestor_synctime(indexer, o, false, 0);

        return o;
    }

    @Override
    public WnObj get(String id) {
        WnObjMapping om = mappings.checkById(id);
        WnIoIndexer indexer = om.getSelfIndexer();
        WnObj o = indexer.get(om.getMyId());
        return Wn.WC().whenAccess(o, true);
    }

    @Override
    public WnObj getOne(WnQuery q) {
        final WnObj[] re = new WnObj[1];
        if (q == null)
            q = new WnQuery();
        q.limit(1);

        each(q, new Each<WnObj>() {
            public void invoke(int index, WnObj obj, int length) {
                re[0] = obj;
            }
        });
        return re[0];
    }

    @Override
    public WnObj getRoot() {
        return mappings.getRoot();
    }

    @Override
    public String getRootId() {
        return mappings.getRoot().id();
    }

    @Override
    public boolean isRoot(String id) {
        return mappings.getRoot().isSameId(id);
    }

    @Override
    public boolean isRoot(WnObj o) {
        return mappings.getRoot().isSameId(o);
    }

    @Override
    public int each(WnQuery q, Each<WnObj> callback) {
        // 声明了 ID 转到 get(id)
        String id = q.first().getString("id");
        if (!Strings.isBlank(id)) {
            WnObjMapping om = mappings.checkById(id);
            WnIoIndexer indexer = om.getSelfIndexer();
            WnObj o = indexer.get(id);
            if (null == o) {
                return 0;
            }
            callback.invoke(0, o, 1);
            return 1;
        }

        // 准备回调
        Each<WnObj> looper = Wn.eachLooping(callback);

        // 如果声明了 pid ，则看看有木有映射
        String pid = q.first().getString("pid");
        if (!Strings.isBlank(pid)) {
            WnObjMapping om = mappings.checkById(pid);
            WnIoIndexer indexer = om.getSubIndexer();
            // 确保 pid 是子ID
            q.setv("pid", om.getMyId());
            return indexer.each(q, looper);
        }
        // 采用根索引管理器
        WnIoIndexer indexer = mappings.getGlobalIndexer();
        return indexer.each(q, looper);
    }

    @Override
    public List<WnObj> query(WnQuery q) {
        List<WnObj> list = new LinkedList<>();
        this.each(q, new Each<WnObj>() {
            public void invoke(int index, WnObj obj, int length) {
                list.add(obj);
            }
        });
        return list;
    }

    @Override
    public List<WnObj> getChildren(WnObj o, String name) {
        // 确保解开了链接
        o = Wn.WC().whenEnter(o, false);

        // 查询
        WnIoMapping mapping = mappings.checkMapping(o);
        WnIoIndexer indexer = mapping.getIndexer();
        return indexer.getChildren(o, name);
    }

    @Override
    public long count(WnQuery q) {
        // 声明了 ID 转到 get(id)
        String id = q.first().getString("id");
        if (!Strings.isBlank(id)) {
            WnObjMapping om = mappings.checkById(id);
            WnIoIndexer indexer = om.getSelfIndexer();
            WnObj o = indexer.get(id);
            if (null == o) {
                return 0;
            }
            return 1;
        }

        // 如果声明了 pid ，则看看有木有映射
        String pid = q.first().getString("pid");
        if (!Strings.isBlank(pid)) {
            WnObjMapping om = mappings.checkById(pid);
            WnIoIndexer indexer = om.getSubIndexer();
            // 确保 pid 是子ID
            q.setv("pid", om.getMyId());
            return indexer.count(q);
        }
        // 采用根索引管理器
        WnIoIndexer indexer = mappings.getGlobalIndexer();
        return indexer.count(q);
    }

    @Override
    public boolean hasChild(WnObj p) {
        WnQuery q = Wn.Q.pid(p);
        long n = this.count(q);
        return n > 0;
    }

    @Override
    public WnObj push(String id, String key, Object val, boolean returnNew) {
        WnObjMapping om = mappings.checkById(id);
        WnIoIndexer indexer = om.getSelfIndexer();
        return indexer.push(om.getMyId(), key, val, returnNew);
    }

    @Override
    public void push(WnQuery q, String key, Object val) {
        // 声明了 ID 转到 get(id)
        String id = q.first().getString("id");
        if (!Strings.isBlank(id)) {
            WnObjMapping om = mappings.checkById(id);
            WnIoIndexer indexer = om.getSelfIndexer();
            indexer.push(om.getMyId(), key, val, false);
            return;
        }

        // 如果声明了 pid ，则看看有木有映射
        String pid = q.first().getString("pid");
        if (!Strings.isBlank(pid)) {
            WnObjMapping om = mappings.checkById(pid);
            WnIoIndexer indexer = om.getSubIndexer();
            // 确保 pid 是子ID
            q.setv("pid", om.getMyId());

            indexer.push(q, key, val);
            return;
        }
        // 采用根索引管理器
        WnIoIndexer indexer = mappings.getGlobalIndexer();
        indexer.push(q, key, val);
    }

    @Override
    public WnObj pull(String id, String key, Object val, boolean returnNew) {
        WnObjMapping om = mappings.checkById(id);
        WnIoIndexer indexer = om.getSelfIndexer();
        return indexer.pull(om.getMyId(), key, val, returnNew);
    }

    @Override
    public void pull(WnQuery q, String key, Object val) {
        // 声明了 ID 转到 get(id)
        String id = q.first().getString("id");
        if (!Strings.isBlank(id)) {
            WnObjMapping om = mappings.checkById(id);
            WnIoIndexer indexer = om.getSelfIndexer();
            indexer.pull(om.getMyId(), key, val, false);
            return;
        }

        // 如果声明了 pid ，则看看有木有映射
        String pid = q.first().getString("pid");
        if (!Strings.isBlank(pid)) {
            WnObjMapping om = mappings.checkById(pid);
            WnIoIndexer indexer = om.getSubIndexer();
            // 确保 pid 是子ID
            q.setv("pid", om.getMyId());

            indexer.pull(q, key, val);
            return;
        }
        // 采用根索引管理器
        WnIoIndexer indexer = mappings.getGlobalIndexer();
        indexer.pull(q, key, val);
    }

    @Override
    public void setMount(WnObj o, String mnt) {
        mnt = Strings.sBlank(mnt, null);
        // 必须操作映射根节点
        // 即，这个节点是 mount 但是 mountRootId 又为空，那么它就是映射的根节点
        // 如果没有映射的节点，当然也能修改映射
        String mntId = o.mountRootId();
        if (!o.isMount() || (null == mntId)) {
            // 取消映射:
            if (null == mnt) {
                if (o.isMount()) {
                    appendMeta(o, new NutMap("!mnt", ""));
                    o.remove("mnt");
                }
            }
            // 设置映射
            else {
                o.setv("mnt", mnt);
                set(o, "^mnt$");
            }
        }
        // 否则抛个错
        else {
            throw Er.create("e.io.mount.NotRootNode", o.path());
        }
    }

    @SuppressWarnings("unchecked")
    private NutBean __any_to_map(WnObj o, Object meta) {
        // 防守一下
        if (null == meta)
            return null;
        // 转成 Map
        NutBean map = null;
        // 字符串
        if (meta instanceof CharSequence) {
            String str = meta.toString();
            // 是一个正则表达式
            if (str.startsWith("!^") || str.startsWith("^")) {
                map = o.pickBy(str);
            }
            // 当作 JSON
            else {
                map = Lang.map(str);
            }
        }
        // 就是 Map
        else if (meta instanceof Map) {
            map = NutMap.WRAP((Map<String, Object>) meta);
        }
        // 其他的统统不支持
        else {
            throw Er.create("e.io.meta.unsupport", meta.getClass().getName());
        }

        return map;
    }

    private void __save_map_for_update_meta(NutBean map) {
        map.pickAndRemoveBy("^(ph|id|race|ct|d[0-9])$");
    }

    @Override
    public void writeMeta(WnObj o, Object meta) {
        // 转换
        NutBean map = __any_to_map(o, meta);

        // 防守
        if (null == map || map.isEmpty()) {
            return;
        }

        // 循环对象
        for (String key : o.keySet()) {
            // id 等是绝对不可以改的
            if (key.matches("^(ph|id|race|ct|lm|sha1|data|d[0-9])$")) {
                continue;
            }
            // 如果不存在，就去掉，因为这是 write
            if (!map.containsKey(key)) {
                // 内置属性，不要去掉
                if (key.matches("^(nm|pid|c|m|g|md|tp|mime|ln|mnt|expi|width|height)$"))
                    continue;
                // 非内置属性，去掉
                map.put("!" + key, true);
            }
        }

        // 确保有最后修改时间
        map.put("lm", System.currentTimeMillis());

        // 执行写入
        WnObj o2 = this.setBy(o.id(), map, true);
        o.clear();
        o.update2(o2);
    }

    @Override
    public void appendMeta(WnObj o, Object meta) {
        // 转换
        NutBean map = __any_to_map(o, meta);

        // 防守
        if (null == map || map.isEmpty()) {
            return;
        }

        // 确保有最后修改时间
        map.put("lm", System.currentTimeMillis());

        // 执行写入
        WnObj o2 = this.setBy(o.id(), map, true);
        o.clear();
        o.update2(o2);
    }

    @Override
    public InputStream getInputStream(WnObj o, long off) {
        // 检查权限同时展开链接为成真正的文件
        o = Wn.WC().whenRead(o, false);

        // 获取映射
        try {
            WnIoMapping im = mappings.checkMapping(o);
            WnIoHandle h;
            // 从头读
            if (off == 0) {
                h = im.open(o, Wn.S.R);
            }
            // 在某位置读
            else {
                h = im.open(o, Wn.S.RW);
                h.seek(off);
            }
            h.setOffset(off);
            return new WnIoInputStream(h);
        }
        catch (Exception e) {
            throw Er.wrap(e);
        }
    }

    @Override
    public OutputStream getOutputStream(WnObj o, long off) {
        // 检查权限同时展开链接为成真正的文件
        o = Wn.WC().whenWrite(o, false);

        try {
            WnIoMapping im = mappings.checkMapping(o);
            WnIoHandle h;
            // 从头写
            if (off == 0) {
                h = im.open(o, Wn.S.W);
            }
            // 在某位置写
            else {
                h = im.open(o, Wn.S.WM);
                h.seek(off);
            }
            return new WnIoOutputStream(h, this.whenWrite);
        }
        catch (Exception e) {
            throw Er.wrap(e);
        }
    }

    @Override
    public String readText(WnObj o) {
        InputStream ins = null;
        Reader r = null;
        ins = this.getInputStream(o, 0);
        r = Streams.buffr(Streams.utf8r(ins));
        return Streams.readAndClose(r);
    }

    @Override
    public BufferedImage readImage(WnObj o) {
        InputStream ins = null;
        try {
            ins = this.getInputStream(o, 0);
            InputStream bins = Streams.buff(ins);
            return ImageIO.read(bins);
        }
        catch (IOException e) {
            throw Er.create("e.io.read.img", o);
        }
        finally {
            Streams.safeClose(ins);
        }
    }

    @Override
    public long readAndClose(WnObj o, OutputStream ops) {
        InputStream ins = this.getInputStream(o, 0);
        return Streams.writeAndClose(ops, ins);
    }

    @Override
    public <T> T readJson(WnObj o, Class<T> classOfT) {
        InputStream ins = null;
        Reader r = null;
        try {
            ins = this.getInputStream(o, 0);
            r = Streams.buffr(Streams.utf8r(ins));
            return Json.fromJson(classOfT, r);
        }
        finally {
            Streams.safeClose(r);
            Streams.safeClose(ins);
        }
    }

    @Override
    public long writeImage(WnObj o, RenderedImage im) {
        OutputStream ops = null;
        try {
            ops = this.getOutputStream(o, 0);
            Images.write(im, o.type(), ops);
            return o.len();
        }
        finally {
            Streams.safeClose(ops);
        }
    }

    @Override
    public long writeText(WnObj o, CharSequence cs) {
        byte[] b = cs.toString().getBytes(Encoding.CHARSET_UTF8);
        OutputStream ops = null;
        try {
            ops = this.getOutputStream(o, 0);
            ops.write(b);
        }
        catch (IOException e) {
            throw Lang.wrapThrow(e);
        }
        finally {
            Streams.safeClose(ops);
        }
        return o.len();
    }

    @Override
    public long appendText(WnObj o, CharSequence cs) {
        byte[] b = cs.toString().getBytes(Encoding.CHARSET_UTF8);
        OutputStream ops = null;
        try {
            ops = this.getOutputStream(o, -1);
            ops.write(b);
        }
        catch (IOException e) {
            throw Lang.wrapThrow(e);
        }
        finally {
            Streams.safeClose(ops);
        }
        return o.len();
    }

    @Override
    public long writeJson(WnObj o, Object obj, JsonFormat fmt) {
        if (null == fmt)
            fmt = JsonFormat.full().setQuoteName(true);

        Object json = obj;
        if (obj instanceof CharSequence) {
            json = Json.fromJson(obj.toString());
        }
        OutputStream ops = null;
        Writer w = null;
        try {
            ops = this.getOutputStream(o, 0);
            w = Streams.buffw(Streams.utf8w(ops));
            Json.toJson(w, json, fmt);
        }
        finally {
            Streams.safeClose(w);
            Streams.safeClose(ops);
        }
        return o.len();
    }

    @Override
    public long writeAndClose(WnObj o, InputStream ins) {
        OutputStream ops = this.getOutputStream(o, 0);
        return Streams.writeAndClose(ops, ins);
    }

    @Override
    public Reader getReader(WnObj o, long off) {
        InputStream ins = this.getInputStream(o, off);
        return Streams.utf8r(ins);
    }

    @Override
    public Writer getWriter(WnObj o, long off) {
        OutputStream ops = this.getOutputStream(o, off);
        return Streams.utf8w(ops);
    }

    @Override
    public MimeMap mimes() {
        return mappings.getGlobalIndexer().mimes();
    }

    @Override
    public void _clean_for_unit_test() {
        throw Lang.noImplement();
    }

}
