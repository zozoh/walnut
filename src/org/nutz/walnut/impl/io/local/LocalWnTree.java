package org.nutz.walnut.impl.io.local;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import org.nutz.lang.ContinueLoop;
import org.nutz.lang.Each;
import org.nutz.lang.ExitLoop;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnNode;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.api.io.WnTree;
import org.nutz.walnut.api.io.WnTreeFactory;
import org.nutz.walnut.impl.io.AbstractWnTree;
import org.nutz.walnut.util.Wn;

public class LocalWnTree extends AbstractWnTree {

    private File home;

    private MemNodeMap mnm;

    public LocalWnTree(WnTreeFactory factory) {
        super(factory);
    }

    @Override
    public void setTreeNode(WnNode treeNode) {
        super.setTreeNode(treeNode);
        // 根据节点获取本地顶级目录
        String mnt = treeNode.mount();
        if (!mnt.startsWith("file://"))
            throw Er.create("e.io.tree.local.invalid.mnt", mnt);
        String ph = mnt.substring("file://".length());
        home = Files.createDirIfNoExists(ph);
        if (!home.isDirectory())
            throw Er.create("e.io.tree.local.invalid.home", home);

        // 分析自己的节点
        mnm = new MemNodeMap();
        File f = _get_tree_cache_file();
        mnm.loadAndClose(Streams.fileInr(f));
    }

    private File _get_tree_cache_file() {
        File f = Files.getFile(home, ".wn/tree");
        Files.createFileIfNoExists(f);
        return f;
    }

    private String _check_rpath(File f) {
        String base = home.getAbsolutePath();
        String ph = f.getAbsolutePath();
        if (!ph.startsWith(base)) {
            throw Er.create("e.io.tree.local.OutOfPath", ph + " <!> " + base);
        }
        return ph.substring(base.length() + 1);
    }

    private String _check_local_path(WnNode nd) {
        if (isTreeNode(nd.id()))
            return home.getAbsolutePath();

        if (!nd.tree().equals(this))
            throw Er.create("e.io.tree.local.OutOfMount", nd);

        String ph = nd.path();
        if (!ph.startsWith(rootPath))
            throw Er.create("e.io.tree.local.OutOfPath", ph + " <!> " + rootPath);

        String re;

        // 顶级树
        if (rootPath.equals("/")) {
            re = ph;
        }
        // 非顶级树，那么看看相对路径
        else {
            re = ph.substring(rootPath.length());
            if (!re.startsWith("/"))
                throw Er.create("e.io.tree.local.OutOfDir", ph + " <!> " + rootPath);
        }

        return home.getAbsolutePath() + re;
    }

    private File _check_local_file(String rpath) {
        File f = Files.getFile(home, rpath);
        if (!f.exists())
            throw Er.create("e.io.tree.nd.noexists", rpath);
        return f;
    }

    private File _get_local_file(WnNode nd) {
        // 根
        if (isTreeNode(nd.id())) {
            return home;
        }
        // 本地节点
        else if (nd instanceof LocalWnNode) {
            return ((LocalWnNode) nd).getFile();
        }
        // 其他节点
        return new File(_check_local_path(nd));
    }

    private File _check_local_file(WnNode nd) {
        File f = _get_local_file(nd);
        if (null == f || !f.exists())
            throw Er.create("e.io.tree.nd.noexists", nd);
        return f;
    }

    WnNode _file_to_node(File f, String id, boolean autoGenIndex) {
        // 创建节点
        LocalWnNode nd = new LocalWnNode(f);
        nd.setTree(this);

        // 得到相对路径
        String rpath = _check_rpath(f);

        // 首先试图根据 ID 获取一下索引
        MemNodeItem mni = null;
        if (!Strings.isBlank(id)) {
            mni = mnm.getById(id);
        }

        // 没有这个索引说明什么呢？
        // 可能是 id==null 或者索引根本不存在
        // 不过怎么样，根据 rpath 再拿一下看看
        if (null == mni) {
            mni = mnm.getByPath(rpath);
        }
        // 如果获取到这个索引，那么更新一下 path
        // 只有调用者应该会 refresh_buffer 的吧
        else {
            mni.path = rpath;
        }

        // 还是 null 则表示根本没有这个节点的索引
        if (null == mni) {
            // 如果不自动生成索引，那么就抛错
            if (!autoGenIndex)
                throw Er.create("e.io.tree.noindex", f);

            // 否则就自动生成索引
            if (Strings.isBlank(id))
                nd.id(Wn.genId());
            else
                nd.id(id);
            mnm.add(nd.id() + ":" + rpath);
        }
        // 找到了索引就用索引填充节点对应的字段
        else {
            nd.id(mni.id);
            nd.mount(mni.mount);
        }
        // 最后补全路径，收工
        nd.path(rootPath).appendPath(rpath);
        return nd;
    }

    private LocalWnNode _get_node(MemNodeItem mni) {
        String rpath = mni.path;
        File f = _check_local_file(rpath);
        LocalWnNode nd = new LocalWnNode(f);
        nd.id(mni.id);
        nd.path(rootPath).appendPath(rpath);
        nd.mount(mni.mount);
        return nd;
    }

    @Override
    public int eachMountTree(Each<WnTree> callback) {
        int i = 0;
        int n = 0;
        for (MemNodeItem mni : mnm.mounts()) {
            WnNode nd = _get_node(mni);

            WnTree tree = factory().check(nd);

            // 虽然不太可能，但是还是判断一下防止无穷递归吧。
            if (tree == this)
                continue;

            // 调用回调并计数
            try {
                callback.invoke(i++, tree, n);
            }
            catch (ExitLoop e) {
                break;
            }
            catch (ContinueLoop e) {}
            finally {
                n++;
            }
        }
        return n;
    }

    @Override
    protected WnNode _fetch_one_by_name(WnNode p, String name) {
        File d = _check_local_file(p);
        File f = Files.getFile(d, name);
        if (!f.exists())
            return null;

        return this._file_to_node(f, null, false);
    }

    @Override
    public boolean exists(WnNode p, String name) {
        File d = _check_local_file(p);
        File f = Files.getFile(d, name);
        return f.exists();
    }

    @Override
    protected int _each_children(WnNode p, String str, Each<WnNode> callback) {
        // 得到相对路径
        File d = _check_local_file(p);

        if (!d.isDirectory())
            throw Er.create("e.io.tree.local.shouldBeDir", str);

        // 设置名称过滤条件
        Pattern pat = null;
        if (!Strings.isBlank(str)) {
            if (str.startsWith("^")) {
                pat = Pattern.compile(str);
            }
            // 看看是通配符还是普通名字
            else {
                String s = str.replace("*", ".*");
                // 通配符的话，变正则表达式
                if (!s.equals(str)) {
                    pat = Pattern.compile("^" + s);
                }
            }
        }

        // 直接是名字
        if (null == pat && null != str) {
            File f = Files.getFile(d, str);
            if (!f.exists())
                return 0;
            WnNode nd = _file_to_node(f, null, false);
            nd.setParent(p);

            try {
                callback.invoke(0, nd, 1);
            }
            catch (ExitLoop e) {}
            catch (ContinueLoop e) {}
            // 返回计数 1
            return 1;
        }

        // 列目录啊列目录
        int nb = 0;
        File[] fileList = d.listFiles();
        for (File f : fileList) {
            // 忽略顶级节点索引目录
            if (f.getName().equals(".wn"))
                continue;
            // 过滤
            if (null != str)
                if (!pat.matcher(f.getName()).find())
                    continue;

            WnNode nd = _file_to_node(f, null, false);

            // 计数并调用回调
            try {
                callback.invoke(nb, nd, fileList.length);
            }
            catch (ExitLoop e) {
                break;
            }
            catch (ContinueLoop e) {}
            finally {
                nb++;
            }
        }

        return nb;
    }

    @Override
    public boolean hasChildren(WnNode nd) {
        File d = this._check_local_file(nd);
        return d.isDirectory() && d.list().length > 0;
    }

    @Override
    protected WnNode _create_node(WnNode p, String id, String name, WnRace race) {
        // 首先，咱不支持 OBJ，因为本地文件木法表达
        if (race == WnRace.OBJ)
            throw Er.create("e.io.tree.local.OBJ");

        if (null == id)
            id = Wn.genId();

        // 得到本地目录
        File d = _check_local_file(p);

        // 必须是一个目录
        if (!d.isDirectory())
            throw Er.create("e.io.tree.local.mustbedir", p);

        // 展开名称
        name = Wn.evalName(name, id);

        // 创建对象前先检查重名
        File f = Files.getFile(d, name);
        if (f.exists()) {
            throw Er.create("e.io.tree.local.nd.exists", f);
        }
        // 文件
        if (WnRace.FILE == race) {
            Files.createFileIfNoExists(f);
        }
        // 目录
        else if (WnRace.DIR == race) {
            Files.createDirIfNoExists(f);
        }
        // 不可能
        else {
            throw Lang.impossible();
        }

        return _file_to_node(f, id, true);
    }

    @Override
    protected void _delete_self(WnNode nd) {
        // 删除索引
        if (nd.hasID())
            mnm.removeById(nd.id());

        // 删除文件
        File f = _get_local_file(nd);
        if (null != f && f.exists()) {
            if (f.isDirectory()) {
                Files.deleteDir(f);
            } else if (f.isFile()) {
                Files.deleteFile(f);
            } else {
                throw Lang.impossible();
            }
        }
    }

    @Override
    protected WnNode _do_rename(WnNode nd, String newName) {
        File f = _check_local_file(nd);
        Files.rename(f, newName);

        // 重新获取文件
        File f2 = new File(f.getParent() + "/" + newName);

        // 修改索引项目，因为指定了 ID，如果索引有这个 ID 的话，只会更新
        return this._file_to_node(f2, nd.id(), false);
    }

    @Override
    protected WnNode _do_append(WnNode p, WnNode nd) {
        File fp = _get_local_file(p);
        File fnd = _get_local_file(nd);

        File fdest = Files.getFile(fp, fnd.getName());
        try {
            Files.move(fnd, fdest);
        }
        catch (IOException e) {
            throw Lang.wrapThrow(e);
        }

        // 修改索引项目，因为指定了 ID，如果索引有这个 ID 的话，只会更新
        return this._file_to_node(fdest, nd.id(), false);
    }

    @Override
    public WnNode _do_set_mount(WnNode nd, String mnt) {
        MemNodeItem mni = mnm.getById(nd.id());
        if (null == mni)
            throw Er.create("e.io.tree.local.nd.noexists", nd);
        mni.mount = mnt;
        return nd;
    }

    @Override
    public void _clean_for_unit_test() {
        Files.clearDir(home);
        mnm.clear();
        this._flush_buffer();
    }

    @Override
    protected LocalWnNode _get_my_node(String id) {
        MemNodeItem mni = mnm.getById(id);
        if (null == mni)
            return null;
        return _get_node(mni);
    }

    protected void _flush_buffer() {
        File f = _get_tree_cache_file();
        mnm.writeAndClose(Streams.fileOutw(f));
    }

}
