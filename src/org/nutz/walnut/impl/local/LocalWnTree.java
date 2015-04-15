package org.nutz.walnut.impl.local;

import java.io.File;
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
import org.nutz.walnut.impl.AbstractWnTree;

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
        if (treeNode.isSameId(nd))
            return home.getAbsolutePath();

        if (!nd.tree().equals(this))
            throw Er.create("e.io.tree.local.OutOfMount", nd);

        String ph = nd.path();
        if (!ph.startsWith(rootPath))
            throw Er.create("e.io.tree.local.OutOfPath", ph + " <!> " + rootPath);

        String re = ph.substring(rootPath.length());
        if (!re.startsWith("/"))
            throw Er.create("e.io.tree.local.OutOfDir", ph + " <!> " + rootPath);

        return home.getAbsolutePath() + re;
    }

    private File _check_local_file(String rpath) {
        File f = Files.getFile(home, rpath);
        if (!f.exists())
            throw Er.create("e.io.tree.nd.noexists", rpath);
        return f;
    }

    private File _get_local_file(WnNode nd) {
        if (nd == treeNode)
            return home;
        if (nd instanceof LocalWnNode) {
            return ((LocalWnNode) nd).getFile();
        }
        return new File(_check_local_path(nd));
    }

    private File _check_local_file(WnNode nd) {
        File f = _get_local_file(nd);
        if (null == f || !f.exists())
            throw Er.create("e.io.tree.nd.noexists", nd);
        return f;
    }

    private WnNode _check_node(File f) {
        // 创建节点
        LocalWnNode nd = new LocalWnNode(f);
        nd.setTree(this);

        // 得到相对路径
        String rpath = _check_rpath(f);

        // TODO 从这里寻找对应的 ID，没有就生成一个
        MemNodeItem mni = mnm.getByPath(rpath);
        if (null == mni) {
            nd.genID();
            mnm.add(nd.id() + ":" + rpath);
        } else {
            nd.id(mni.id);
            nd.mount(mni.mount);
        }
        nd.path(rootPath + "/" + rpath);
        return nd;
    }

    private WnNode _get_node(MemNodeItem mni) {
        String rpath = mni.path;
        File f = _check_local_file(rpath);
        LocalWnNode nd = new LocalWnNode(f);
        nd.setTree(this);
        nd.id(mni.id);
        nd.path(rootPath + "/" + rpath);
        nd.mount(mni.mount);
        return nd;
    }

    @Override
    public int eachMountTree(Each<WnTree> callback) {
        int i = 0;
        int n = 0;
        for (MemNodeItem mni : mnm.mounts()) {
            WnNode nd = _get_node(mni);

            WnTree tree = factory().check(nd.path(), nd.mount());

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
    public int eachChildren(WnNode p, String str, Each<WnNode> callback) {
        if (null == p)
            p = treeNode;

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
        if (null == pat) {
            File f = Files.getFile(d, str);
            if (!f.exists())
                return 0;
            WnNode nd = _check_node(f);
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
            // 过滤
            if (!pat.matcher(f.getName()).find())
                continue;

            WnNode nd = _check_node(f);
            nd.setParent(p);

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
    public WnNode create_node(WnNode p, String name, WnRace race) {
        // 首先，咱不支持 OBJ，因为本地文件木法表达
        if (race == WnRace.OBJ)
            throw Er.create("e.io.tree.local.OBJ");

        p = this.check_parent(p, race);

        File d;
        if (p == treeNode) {
            d = home;
        } else {
            d = ((LocalWnNode) p).getFile();
        }

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

        return _check_node(f);
    }

    @Override
    protected void delete_self(WnNode nd) {
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
    public void rename(WnNode nd, String newName) {
        LocalWnNode lnd = (LocalWnNode) nd;
        Files.rename(lnd.getFile(), newName);
    }

    @Override
    public void setMount(WnNode nd, String mnt) {
        MemNodeItem mni = mnm.getById(nd.id());
        if (null == mni)
            throw Er.create("e.io.tree.local.nd.noexists", nd);
        mni.mount = mnt;
        nd.mount(mnt);
        mnm.mount(mni);
        this._flush_buffer();
    }

    @Override
    public void _clean_for_unit_test() {
        Files.clearDir(home);
        mnm.clear();
        this._flush_buffer();
    }

    @Override
    public WnNode get_my_node(String id) {
        MemNodeItem mni = mnm.getById(id);
        if (null == mni)
            return null;
        return _get_node(mni);
    }

    @Override
    public WnNode loadParents(WnNode nd, boolean force) {
        if (treeNode.isSameId(nd)) {
            if (treeNode != nd) {
                nd.setParent(treeNode.parent());
            }
            return nd;
        }
        if (null == nd.parent() || force) {
            LocalWnNode lnd = (LocalWnNode) nd;
            WnNode p = this._check_node(lnd.getFile().getParentFile());
            loadParents(p, force);
            lnd.setParent(p);
        }
        return nd;
    }

    protected void _flush_buffer() {
        File f = _get_tree_cache_file();
        mnm.writeAndClose(Streams.fileOutw(f));
    }

}
