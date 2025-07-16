package com.site0.walnut.core.io;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

import org.nutz.img.Images;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Each;
import org.nutz.lang.Encoding;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Callback;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.MimeMap;
import com.site0.walnut.api.io.WalkMode;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnIoIndexer;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnObjFilter;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.api.io.WnSecurity;
import com.site0.walnut.api.io.agg.WnAggOptions;
import com.site0.walnut.api.io.agg.WnAggResult;
import com.site0.walnut.api.lock.WnLockApi;
import com.site0.walnut.core.WnIoActionCallback;
import com.site0.walnut.core.WnIoHandle;
import com.site0.walnut.core.WnIoHandleMutexException;
import com.site0.walnut.core.WnIoMapping;
import com.site0.walnut.core.WnIoMappingFactory;
import com.site0.walnut.core.bean.WnObjId;
import com.site0.walnut.core.bean.WnObjMapping;
import com.site0.walnut.core.stream.WnIoInputStream;
import com.site0.walnut.core.stream.WnIoOutputStream;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.WnContext;
import com.site0.walnut.util.Ws;

public class WnIoImpl2 implements WnIo {

    private static final Log log = Wlog.getIO();

    /**
     * 锁服务
     */
    private WnLockApi locks;

    @Override
    public WnLockApi getLockApi() {
        return locks;
    }

    /**
     * 映射工厂类
     */
    private WnIoMappingFactory mappings;

    /**
     * 子类或包裹类可以添加一个删除动作的回调。
     * <p>
     * 因为删除动作可能涉及递归深层删除，因此不能用在子类函数覆盖super方法进行拦截
     */
    protected WnIoActionCallback whenDelete;

    /**
     * 子类或包裹类可以设置一个写操作的回调。
     * <p>
     * 在打开输出流时，会附加这个回调，当流关闭时，也会调用。
     * <p>
     * !!! 注意，直接采用句柄操作，则不会触发这个回调
     */
    protected WnIoActionCallback whenWrite;

    public WnIoImpl2() {}

    public WnIoImpl2(WnIoMappingFactory mappings) {
        this.mappings = mappings;
    }

    public void setMappings(WnIoMappingFactory mappings) {
        this.mappings = mappings;
    }

    public void setWhenDelete(WnIoActionCallback whenDelete) {
        this.whenDelete = whenDelete;
    }

    public void setWhenWrite(WnIoActionCallback whenWrite) {
        this.whenWrite = whenWrite;
    }

    @Override
    public WnIoMappingFactory getMappingFactory() {
        return mappings;
    }

    // 如果不在同样的映射桶内，则，只能通过流 copy 了
    @Override
    public long copyData(WnObj a, WnObj b) {
        try {
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
                throw Wlang.wrapThrow(e);
            }
            catch (WnIoHandleMutexException e) {
                throw Wlang.wrapThrow(e);
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
        // 触发同步时间修改
        finally {
            Wn.Io.update_ancestor_synctime(this, b, false, 0);
        }
    }

    @Override
    public WnIoIndexer getIndexer(WnObj o) {
        WnIoMapping im = mappings.checkMapping(o);
        return im.getIndexer();
    }

    @Override
    public WnIoHandle openHandle(WnObj o, int mode) throws WnIoHandleMutexException, IOException {
        WnIoMapping im = mappings.checkMapping(o);
        WnIoHandle h = im.open(o, mode);
        h.setIo(this);
        return h;
    }

    @Override
    public String open(WnObj o, int mode) {
        throw Wlang.noImplement();
    }

    @Override
    public WnObj flush(String hid) {
        throw Wlang.noImplement();
    }

    @Override
    public WnObj close(String hid) {
        throw Wlang.noImplement();
    }

    @Override
    public int read(String hid, byte[] bs, int off, int len) {
        throw Wlang.noImplement();
    }

    @Override
    public void write(String hid, byte[] bs, int off, int len) {
        throw Wlang.noImplement();
    }

    @Override
    public int read(String hid, byte[] bs) {
        throw Wlang.noImplement();
    }

    @Override
    public void write(String hid, byte[] bs) {
        throw Wlang.noImplement();
    }

    @Override
    public void seek(String hid, long pos) {
        throw Wlang.noImplement();
    }

    @Override
    public long getPos(String hid) {
        throw Wlang.noImplement();
    }

    // 考虑到 copyData 操作除了涉及 BM 也涉及到 indexer，所以主要操作逻辑放到 mapping 层比较合适
    @Override
    public void delete(WnObj o) {
        this.delete(o, false);
    }

    @Override
    public void delete(WnObj o, boolean r) {
        // 确保非空
        if (null == o) {
            o = mappings.getRoot();
        }
        // 确保解开了链接
        else {
            o = Wn.WC().whenRemove(o, false);
        }

        WnIoMapping im;
        // 挂载点，还是采用全局映射管理器
        if (o.isMountEntry()) {
            im = mappings.getGlobalMapping();
        }
        // 其他查询一下应该怎么用映射管理器
        else {
            im = mappings.checkMapping(o);
        }

        im.delete(o, r, this.whenDelete);

        // 更新同步时间
        Wn.Io.update_ancestor_synctime(this, o, false, 0);
    }

    @Override
    public void trancate(WnObj o, long len) {
        // 确保非空
        if (null == o) {
            o = mappings.getRoot();
        }
        // 确保解开了链接
        else {
            o = Wn.WC().whenRemove(o, false);
        }

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
        return im.getIndexer().exists(p, path);
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
            if (null == p) {
                throw Er.create("e.io.obj.noexists", path);
            } else {
                throw Er.create("e.io.obj.noexists", path + ", p:" + p.path());
            }
        return o;
    }

    @Override
    public WnObj fetch(WnObj p, String path) {
        if (null == path)
            return null;

        if (path.startsWith("/")) {
            p = null;
        }

        // 处理挂载节点
        if (null != p && p.isMount()) {
            WnIoMapping mapping = mappings.checkMapping(p);
            return mapping.getIndexer().fetch(p, path);
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
                nd = Wlang.first(this.query(q));
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
        }

        // 目标是通配符或正则表达式
        if (nm.startsWith("^") || nm.contains("*")) {
            WnQuery q = Wn.Q.pid(p).setv("nm", nm).limit(1);
            nd = Wlang.first(this.query(q));
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
        walk(p, callback, mode, null);
    }

    @Override
    public void walk(WnObj p, Callback<WnObj> callback, WalkMode mode, WnObjFilter filter) {
        WnObj p2;
        // 确保非空
        if (null == p) {
            p2 = mappings.getRoot();
        }
        // 确保解开了链接
        else {
            p2 = Wn.WC().whenEnter(p, false);
        }
        WnIoMapping mapping = mappings.checkMapping(p2);
        WnIoIndexer indexer = mapping.getIndexer();
        indexer.walk(p2, callback, mode, filter);
    }

    @Override
    public WnObj move(WnObj src, String destPath) {
        return this.move(src, destPath, Wn.MV.TP | Wn.MV.SYNC);
    }

    @Override
    public WnObj move(WnObj src, String destPath, int mode) {
        // 得到自身的原始的父
        WnObj oldP = src.parent();

        // TODO 这里还没考虑到在不同的映射间怎么移动的问题
        try {
            WnObjMapping om = mappings.checkById(src.id());
            WnIoIndexer indexer = om.getSelfIndexer();
            return indexer.move(src, destPath, mode);
        }
        // 触发同步
        finally {
            if (Wn.MV.isSYNC(mode)) {
                long now = Wn.now();
                // 触发同步时间修改
                Wn.Io.update_ancestor_synctime(this, src, false, now);

                // 如果对象换了父节点，之前的父节点也要被触发修改时间
                if (!oldP.isSameId(src.parentId())) {
                    Wn.Io.update_ancestor_synctime(this, oldP, true, now);
                }
            }
        }
    }

    @Override
    public WnObj rename(WnObj o, String nm) {
        return this.rename(o, nm, false);
    }

    @Override
    public WnObj rename(WnObj o, String nm, boolean keepType) {
        int mode = Wn.MV.SYNC;
        if (!keepType)
            mode |= Wn.MV.TP;
        return this.rename(o, nm, mode);
    }

    @Override
    public WnObj rename(WnObj o, String nm, int mode) {
        // zozoh@2022-10-19: 不知道为啥脑子抽了，应该在 Indexer 里检查
        // IO 层没必要检查名称并路径化
        // Wobj.assertValidName(nm);
        // String ph = o.path();
        // ph = Files.renamePath(ph, nm);

        // 得到自身的原始的父
        WnObj oldP = o.parent();

        // TODO 这里还没考虑到在不同的映射间怎么移动的问题
        try {
            WnObjMapping om = mappings.checkById(o.id());
            WnIoIndexer indexer = om.getSelfIndexer();
            return indexer.rename(o, nm);
        }
        // 触发同步
        finally {
            if (Wn.MV.isSYNC(mode)) {
                long now = Wn.now();
                // 触发同步时间修改
                Wn.Io.update_ancestor_synctime(this, o, false, now);

                // 如果对象换了父节点，之前的父节点也要被触发修改时间
                if (!oldP.isSameId(o.parentId())) {
                    Wn.Io.update_ancestor_synctime(this, oldP, true, now);
                }
            }
        }
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
        WnObj o = indexer.setBy(om.getMyId(), map, returnNew);

        // 修改同步时间戳
        Wn.Io.update_ancestor_synctime(this, o, false, o.lastModified());

        // 搞定
        return o;
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
            WnObj oP = this.get(pid);
            if (null == oP)
                return null;
            WnIoMapping im = mappings.checkMapping(oP);
            WnIoIndexer indexer = im.getIndexer();
            // 确保 pid 是子ID
            q.setv("pid", oP.myId());
            return indexer.setBy(q, map, returnNew);
        }
        // 采用根索引管理器
        WnIoIndexer indexer = mappings.getGlobalIndexer();
        WnObj o = indexer.setBy(q, map, returnNew);

        // 修改同步时间戳
        if (null != o) {
            Wn.Io.update_ancestor_synctime(this, o, false, o.lastModified());
        }
        // 搞定
        return o;
    }

    @Override
    public WnObj setBy(String id, String key, Object val, boolean returnNew) {
        NutMap map = Wlang.map(key, val);
        return this.setBy(id, map, returnNew);
    }

    @Override
    public WnObj setBy(WnQuery q, String key, Object val, boolean returnNew) {
        return this.setBy(q, Wlang.map(key, val), returnNew);
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
            WnObj oP = this.get(pid);
            if (null == oP)
                return -1;
            WnIoMapping im = mappings.checkMapping(oP);
            WnIoIndexer indexer = im.getIndexer();
            // 确保 pid 是子ID
            q.setv("pid", oP.myId());
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
        final WnContext wc = Wn.WC();
        // 默认从自己的根开始
        if (null == p) {
            p = this.getRoot();
        }

        // 判断可进入
        p = wc.whenEnter(p, false);

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

        // 准备创建
        final int rightIndex = toIndex - 1;
        final WnObj p0 = p;
        final WnIoIndexer globalIndexer = mappings.getGlobalIndexer();

        // 已经是映射了
        if (p.isMount()) {
            WnIoMapping mapping = mappings.checkMapping(p);
            WnIoIndexer indexer = mapping.getIndexer();
            if (indexer != globalIndexer) {
                return indexer.create(p, paths, fromIndex, toIndex, race);
            }
        }

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
                    if (indexer != globalIndexer) {
                        return indexer.create(nd, paths, i + 1, toIndex, race);
                    }
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
        final WnContext wc = Wn.WC();
        // null 表示从根路径开始
        if (null == p) {
            p = mappings.getRoot();
        }

        // 判断可进入
        p = wc.whenEnter(p, false);

        // 判断可写
        p = wc.whenWrite(p, false);

        WnIoMapping mapping = mappings.checkMapping(p);
        WnIoIndexer indexer = mapping.getIndexer();
        WnObj o = indexer.createById(p, id, name, race);

        // 更新父节点同步时间
        Wn.Io.update_ancestor_synctime(this, o, false, 0);

        return o;
    }

    @Override
    public WnObj create(WnObj p, WnObj o) {
        final WnContext wc = Wn.WC();
        // 首先父不能为空
        if (null == p) {
            p = this.getRoot();
        }

        // 判断可进入
        p = wc.whenEnter(p, false);

        // 判断可写
        p = wc.whenWrite(p, false);

        // 确保自己是父的子
        o.setParent(p);

        // 创建吧
        WnIoMapping mapping = mappings.checkMapping(p);
        WnIoIndexer indexer = mapping.getIndexer();
        o = indexer.create(p, o);
        o.put("__is_created", true);

        // 更新父节点同步时间
        Wn.Io.update_ancestor_synctime(this, o, false, 0);

        return o;
    }

    @Override
    public WnObj createIfNoExists(WnObj p, WnObj o) {
        WnObj o2;
        // 如果未指定名称，则不会去重
        if (Ws.isBlank(o.name())) {
            return this.create(p, o);
        }

        // 首先父不能为空
        if (null == p) {
            p = this.getRoot();
        }

        // 尝试读取
        o2 = this.fetch(p, o.name());

        // 存在就更新
        if (null != o2) {
            WnRace race = o.race();
            if (race != null && race != o2.race()) {
                throw Er.create("e.io.create.invalid.race", o2 + " ! " + race);
            }
            this.appendMeta(o2, o);
            // 更新父节点同步时间
            Wn.Io.update_ancestor_synctime(this, o2, false, 0);
        }
        // 不存在，就创建
        else {
            o2 = this.create(p, o);
        }

        return o2;
    }

    @Override
    public WnObj createIfExists(WnObj p, WnObj o) {
        WnObj o2;
        // 如果未指定名称，则不会去重
        if (Ws.isBlank(o.name())) {
            return this.create(p, o);
        }

        // 首先父不能为空
        if (null == p) {
            p = this.getRoot();
        }

        // 尝试读取
        o2 = this.fetch(p, o.name());

        // 存在就先删除
        if (null != o2) {
            this.delete(o2);
        }
        // 先删除再创建
        o2 = this.create(p, o);

        return o2;
    }

    @Override
    public WnObj createIfNoExists(WnObj p, String path, WnRace race) {
        // null 表示从根路径开始
        if (null == p || path.startsWith("/")) {
            p = mappings.getRoot();
        }
        WnObj o = this.fetch(p, path);

        // 存在就返回
        if (null != o) {
            // 种类冲突，不能忍啊
            if (!o.isRace(race))
                throw Er.create("e.io.create.invalid.race", path + " ! " + race);
            return o;
        }
        // 不存在，就创建
        o = this.create(p, path, race);
        o.put("__is_created", true);

        return o;
    }

    @Override
    public WnObj createIfExists(WnObj p, String path, WnRace race) {
        // null 表示从根路径开始
        if (null == p || path.startsWith("/")) {
            p = mappings.getRoot();
        }

        WnObj o = this.fetch(p, path);
        // 如果存在，删了以便创建新的
        if (null != o) {
            this.delete(o);
        }
        // 先删除再创建
        o = this.create(p, path, race);

        return o;
    }

    @Override
    public WnObj get(String id) {
        WnObjMapping om = mappings.checkById(id);
        WnIoIndexer indexer = om.getSelfIndexer();
        WnObj o = indexer.get(om.getMyId());
        if (null != o) {
            o.mountRootId(om.getHomeId());
        }
        return Wn.WC().whenAccess(o, true);
    }

    public WnObj getIn(WnObj p, String id) {
        if (null == p) {
            return this.get(id);
        }
        WnIoMapping im = mappings.checkMapping(p);
        WnIoIndexer indexer = im.getIndexer();
        WnObj o = indexer.get(id);
        if (null != o) {
            o.mountRootId(p.mountRootId());
        }
        o = Wn.WC().whenAccess(o, true);
        if (o == null)
            return o;

        // 如果 p 是映射的，且 o 不是两段式 ID
        // 那么就说明，在保存的时候，已经删除了父映射的ID，以便精简数据
        // 那么这时读取回来需要加回去，让两段式ID对于客户端是透明的
        if (p.isMount() && !o.hasMountRootId()) {
            WnObjId oid = new WnObjId(p.id(), o.id());
            o.id(oid.toString());
        }

        return o;
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
        // 防止 id:{"$ne":"6nev2ak790imqrf3peb45p4i0f"}
        NutMap qc = q.first();
        String pid = qc.getString("pid");
        Object _id = qc.get("id");
        String id = null;
        if (null != _id && _id instanceof String) {
            id = _id.toString();
            WnObjId oid = new WnObjId(id);
            if (oid.hasHomeId()) {
                pid = oid.getHomeId();
            }
            id = oid.getMyId();
        }

        WnIoIndexer indexer;

        // 准备回调
        Each<WnObj> looper = Wn.eachLooping(callback);

        // 如果声明了 pid ，则看看有木有映射
        if (!Strings.isBlank(pid) && !this.isRoot(pid)) {
            WnObj oP = this.get(pid);
            if (null == oP)
                return 0;

            // 确保父是可进入的
            WnContext wc = Wn.WC();
            oP = wc.whenEnter(oP, true);
            if (null == oP) {
                return 0;
            }

            // 检查映射，尝试交给对应的映射管理器处理
            WnIoMapping im = mappings.checkMapping(oP);
            indexer = im.getIndexer();
            // 确保 pid 是子ID
            q.setv("pid", oP.myId());
            q.setParentObj(oP);
        }
        // 采用根索引管理器
        else {
            indexer = mappings.getGlobalIndexer();
        }

        if (null != id) {
            WnObj o = indexer.get(id);
            if (null == o) {
                return 0;
            }
            callback.invoke(0, o, 1);
            return 1;
        }

        // 采用根索引管理器
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
    public int eachChild(WnObj o, String name, Each<WnObj> callback) {
        // 确保非空
        if (null == o) {
            o = mappings.getRoot();
        }
        // 确保解开了链接
        else {
            o = Wn.WC().whenEnter(o, false);
        }

        // 查询
        WnIoMapping mapping = mappings.checkMapping(o);
        WnIoIndexer indexer = mapping.getIndexer();
        return indexer.eachChild(o, name, callback);
    }

    @Override
    public List<WnObj> getChildren(WnObj o, String name) {
        // 确保非空
        if (null == o) {
            o = mappings.getRoot();
        }
        // 确保解开了链接
        else {
            o = Wn.WC().whenEnter(o, false);
        }

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
        if (!Strings.isBlank(pid) && !this.isRoot(pid)) {
            WnObj oP = this.get(pid);
            if (null == oP)
                return 0;
            WnIoMapping im = mappings.checkMapping(oP);
            WnIoIndexer indexer = im.getIndexer();
            // 确保 pid 是子ID
            q.setv("pid", oP.myId());
            return indexer.count(q);
        }
        // 采用根索引管理器
        WnIoIndexer indexer = mappings.getGlobalIndexer();
        return indexer.count(q);
    }

    @Override
    public WnAggResult aggregate(WnQuery q, WnAggOptions agg) {
        // 如果声明了 pid ，则看看有木有映射
        String pid = q.first().getString("pid");
        if (!Strings.isBlank(pid)) {
            WnObj oP = this.get(pid);
            if (null == oP)
                return new WnAggResult();
            WnIoMapping im = mappings.checkMapping(oP);
            WnIoIndexer indexer = im.getIndexer();
            // 确保 pid 是子ID
            q.setv("pid", oP.myId());
            return indexer.aggregate(q, agg);
        }
        // 采用根索引管理器
        WnIoIndexer indexer = mappings.getGlobalIndexer();
        return indexer.aggregate(q, agg);
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
            WnObj oP = this.get(pid);
            if (null == oP)
                return;
            WnIoMapping im = mappings.checkMapping(oP);
            WnIoIndexer indexer = im.getIndexer();
            // 确保 pid 是子ID
            q.setv("pid", oP.myId());

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
            WnObj oP = this.get(pid);
            if (null == oP)
                return;
            WnIoMapping im = mappings.checkMapping(oP);
            WnIoIndexer indexer = im.getIndexer();
            // 确保 pid 是子ID
            q.setv("pid", oP.myId());

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

    private void __save_map_for_update_meta(NutBean map) {
        map.pickAndRemoveBy("^(ph|id|ct|d[0-9])$");
    }

    @Override
    public void writeMeta(WnObj o, Object meta) {
        // 转换
        NutBean map = Wn.anyToMap(o, meta);

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
        map.put("lm", Wn.now());

        // 执行写入
        WnObj o2 = this.setBy(o.id(), map, true);
        o.clear();
        o.updateBy(o2);
    }

    @Override
    public void appendMeta(WnObj o, Object meta) {
        appendMeta(o, meta, false);
    }

    @Override
    public void appendMeta(WnObj o, Object meta, boolean keepType) {
        // 转换
        NutBean map = Wn.anyToMap(o, meta);

        // 防守
        if (null == map || map.isEmpty()) {
            return;
        }

        // 暗戳戳的改动一下，因为如果要 keepType（nm 不会导致 tp 自动修改）
        // 则，直接在 "nm" 后面增加一个后缀
        if (map.has("nm") && keepType) {
            String nm = map.getString("nm");
            map.remove("nm");
            map.put("nm!", nm);
        }

        // 确保有最后修改时间
        long now = Wn.now();
        map.put("lm", now);

        // 执行写入
        WnObj o2 = this.setBy(o.id(), map, true);
        // zozoh: 为啥要 clear? 在 thing update 的情境下， 这个会导致 th_set/live 等运行时
        // 字段丢失的。 在 dao 映射的场景下，运行时设置的 th_set/live 等属性很重要
        // o.clear();
        o.updateBy(o2);

        // 从对象中移除指定的字段
        for (String key : map.keySet()) {
            if (key.startsWith("!")) {
                String k2 = key.substring(1).trim();
                o.remove(k2);
            }
        }

        // 修改同步时间戳
        Wn.Io.update_ancestor_synctime(this, o, false, now);
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

            // 设置 IO，以便句柄关闭时，更新同步时间
            h.setIo(this);

            // 搞定
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
    public byte[] readBytes(WnObj o) {
        InputStream ins = null;
        try {
            ins = this.getInputStream(o, 0);
            int off = 0;
            int len = (int) o.len();
            byte[] bs = new byte[len];
            int readed;
            while (len > 0 && (readed = ins.read(bs, off, len)) >= 0) {
                if (readed > 0) {
                    off += readed;
                    len -= readed;
                }
            }
            return bs;
        }
        catch (IOException e) {
            throw Er.wrap(e);
        }
        // 安全关闭
        finally {
            Streams.safeClose(ins);
        }

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
            throw Er.create(e, "e.io.read.img", o);
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
            throw Wlang.wrapThrow(e);
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
            throw Wlang.wrapThrow(e);
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
    public long writeBytes(WnObj o, byte[] buf) {
        return writeBytes(o, buf, 0, buf.length);
    }

    @Override
    public long writeBytes(WnObj o, byte[] buf, int off, int len) {
        OutputStream ops = null;
        try {
            ops = this.getOutputStream(o, 0);
            ops.write(buf, off, len);
        }
        catch (IOException e) {
            throw Er.wrap(e);
        }
        finally {
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

}
